package org.plovr;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.javascript.jscomp.Result;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.tofu.SoyTofu;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public final class CompileRequestHandler extends AbstractGetHandler {

  private static final Logger logger = Logger.getLogger(
      CompileRequestHandler.class.getName());

  private static final SoyTofu TOFU;

  static {
    SoyFileSet.Builder builder = new SoyFileSet.Builder();
    builder.add(Resources.getResource(InputFileHandler.class, "raw.soy"));
    SoyFileSet fileSet = builder.build();
    TOFU = fileSet.compileToJavaObj();
  }

  private final Gson gson;

  private final String plovrJsLib;

  public CompileRequestHandler(CompilationServer server) {
    super(server);

    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(CompilationError.class,
        new CompilationErrorSerializer());
    gson = gsonBuilder.create();

    URL plovrJsLibUrl = Resources.getResource("org/plovr/plovr.js");
    String plovrJsLib;
    try {
      plovrJsLib = Resources.toString(plovrJsLibUrl, Charsets.US_ASCII);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error loading errors.js", e);
      plovrJsLib = null;
    }
    this.plovrJsLib = plovrJsLib;
  }

  @Override
  protected void doGet(HttpExchange exchange, QueryData data, Config config) throws IOException {
    // Update these fields as they are responsible for the response that will be
    // written.
    StringBuilder builder = new StringBuilder();
    String contentType;
    int responseCode;

    try {
      if (config.getCompilationMode() == CompilationMode.RAW) {
        Manifest manifest = config.getManifest();
        String js = InputFileHandler.getJsToLoadManifest(
            server, config, manifest, exchange);
        builder.append(js);
      } else {
        compile(config, exchange, builder);
      }
    } catch (MissingProvideException e) {
      Preconditions.checkState(builder.length() == 0,
          "Should not write errors to builder if output has already been written");
      writeErrors(config, ImmutableList.of(e.createCompilationError()),
          builder);
    }
    contentType = "text/javascript";
    responseCode = 200;

    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Content-Type", contentType);
    exchange.sendResponseHeaders(responseCode, builder.length());

    Writer responseBody = new OutputStreamWriter(exchange.getResponseBody());
    responseBody.write(builder.toString());
    responseBody.close();
  }

  public static Compilation compile(Config config)
      throws MissingProvideException, CheckedSoySyntaxException {
    try {
      Compilation compilation = config.getManifest().getCompilerArguments(
          config.getModuleConfig());
      compilation.compile(config);
      return compilation;
    } catch (SoySyntaxException e) {
      throw new CheckedSoySyntaxException(e);
    }
  }

  /**
   * For modes other than RAW, compile the code and write the result to builder.
   * When modules are used, only the code for the initial module will be written,
   * along with the requisite bootstrapping code for the remaining modules.
   */
  private void compile(Config config,
      HttpExchange exchange,
      Appendable appendable) throws IOException, MissingProvideException {
    Compilation compilation;
    try {
      compilation = compile(config);
    } catch (MissingProvideException e) {
      writeErrors(config, ImmutableList.of(e.createCompilationError()),
          appendable);
      return;
    } catch (CheckedSoySyntaxException e) {
      writeErrors(config, ImmutableList.of(e.createCompilationError()),
          appendable);
      return;
    }

    server.recordCompilation(config, compilation);
    Result result = compilation.getResult();

    if (result.success) {
      if (config.getCompilationMode() == CompilationMode.WHITESPACE) {
        appendable.append("CLOSURE_NO_DEPS = true;\n");
      }

      if (compilation.usesModules()) {
        final boolean isDebugMode = true;
        Function<String, String> moduleNameToUri = ModuleHandler.
            createModuleNameToUriConverter(server, exchange, config.getId());
        ModuleConfig moduleConfig = config.getModuleConfig();
        if (moduleConfig.excludeModuleInfoFromRootModule()) {
          // If the module info is excluded from the root module, then the
          // module info should be written out now, followed by JS that will
          // dynamically load the root module.
          compilation.appendRootModuleInfo(appendable, isDebugMode,
              moduleNameToUri);

          String src = moduleNameToUri.apply(moduleConfig.getRootModule());
          SoyMapData mapData = new SoyMapData("src", src);
          final SoyMsgBundle messageBundle = null;
          String js = TOFU.render("org.plovr.loadRootModule", mapData, messageBundle);
          appendable.append(js);
        } else {
          appendable.append(compilation.getCodeForRootModule(isDebugMode,
              moduleNameToUri));
        }
      } else {
        appendable.append(compilation.getCompiledCode());
      }
    }

    // TODO(bolinfest): Check whether writing out the plovr library confuses the
    // source map. Hopefully adding it after the compiled code will prevent it
    // from messing with the line numbers.

    // Write out the plovr library, even if there are no warnings.
    // It is small, and it exports some symbols that may be of use to
    // developers.
    writeErrorsAndWarnings(config, compilation.getCompilationErrors(),
        compilation.getCompilationWarnings(), appendable);
  }

  private void writeErrors(Config config, List<CompilationError> errors,
      Appendable builder) throws IOException {
    writeErrorsAndWarnings(config, errors,
        ImmutableList.<CompilationError>of(),
        builder);
  }

  private void writeErrorsAndWarnings(Config config,
      List<CompilationError> errors, List<CompilationError> warnings,
      Appendable builder) throws IOException {
    Preconditions.checkNotNull(errors);
    Preconditions.checkNotNull(builder);

    String configIdJsString = gson.toJson(config.getId());
    builder.append(plovrJsLib)
        .append("plovr.addErrors(").append(gson.toJson(errors)).append(");\n")
        .append("plovr.addWarnings(").append(gson.toJson(warnings)).append(");\n")
        .append("plovr.setConfigId(").append(configIdJsString).append(");\n")
        .append("plovr.writeErrorsOnLoad();\n");
  }
}

