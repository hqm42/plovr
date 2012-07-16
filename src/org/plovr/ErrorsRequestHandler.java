package org.plovr;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.plovr.io.Responses;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.base.SoySyntaxException;
import com.sun.net.httpserver.HttpExchange;

/**
 * {@link ErrorsRequestHandler} displays the errors that occur during compilation.
 * This is used for development to serve the Javascript in raw mode but still get the neat error messages.
 * This is mostly based on {@link CompileRequestHandler} with some removals.
 * 
 * @author tobias.raeder@gmail.com (Tobias Raeder)
 */
public class ErrorsRequestHandler extends AbstractGetHandler {

  private static final Logger logger = Logger.getLogger(
      ErrorsRequestHandler.class.getName());

  static {
    SoyFileSet.Builder builder = new SoyFileSet.Builder();
    builder.add(Resources.getResource(InputFileHandler.class, "raw.soy"));
  }

  private final Gson gson;

  private final String plovrJsLib;

  public ErrorsRequestHandler(CompilationServer server) {
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

    try {
        compile(config, exchange, builder);
    } catch (CompilationException e) {
      Preconditions.checkState(builder.length() == 0,
          "Should not write errors to builder if output has already been written");
      writeErrors(
          config,
          ImmutableList.of(e.createCompilationError()),
          builder);
    }

    Responses.writeJs(builder.toString(), config, exchange);
  }

  public static Compilation compile(Config config)
      throws CompilationException {
    try {
      Compilation compilation = config.getManifest().getCompilerArguments(
          config.getModuleConfig());
      compilation.compile(config);
      return compilation;
    } catch (SoySyntaxException e) {
      throw new CheckedSoySyntaxException(e);
    } catch (PlovrSoySyntaxException e) {
      throw new CheckedSoySyntaxException(e);
    } catch (PlovrCoffeeScriptCompilerException e) {
      throw new CheckedCoffeeScriptCompilerException(e);
    }
  }

  /**
   * For modes other than RAW, compile the code and write the result to builder.
   * When modules are used, only the code for the initial module will be written,
   * along with the requisite bootstrapping code for the remaining modules.
   */
  private void compile(Config config,
      HttpExchange exchange,
      Appendable appendable) throws IOException, CompilationException {
    Compilation compilation;
    try {
      compilation = compile(config);
    } catch (CompilationException e) {
    	System.out.println("error" + e);
      writeErrors(
          config,
          ImmutableList.of(e.createCompilationError()),
          appendable);
      return;
    }

    server.recordCompilation(config, compilation);

    // TODO(bolinfest): Check whether writing out the plovr library confuses the
    // source map. Hopefully adding it after the compiled code will prevent it
    // from messing with the line numbers.

    // Write out the plovr library, even if there are no warnings.
    // It is small, and it exports some symbols that may be of use to
    // developers.
    writeErrorsAndWarnings(
        config,
        compilation.getCompilationErrors(),
        compilation.getCompilationWarnings(),
        appendable);
  }

  private void writeErrors(
      Config config,
      List<CompilationError> errors,
      Appendable builder)
  throws IOException {
    writeErrorsAndWarnings(
        config,
        errors,
        ImmutableList.<CompilationError>of(),
        builder);
  }

  private void writeErrorsAndWarnings(
      Config config,
      List<CompilationError> errors,
      List<CompilationError> warnings,
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
