package org.seasar.doma.extension.gen;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.seasar.doma.extension.gen.internal.message.Message;
import org.seasar.doma.extension.gen.internal.util.IOUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * ジェネレータです。
 *
 * <p>テンプレートエンジンのFreeMarkerを利用します。
 *
 * <p>
 *
 * @author taedium
 */
public class Generator {

  /** デフォルトのテンプレートディレクトリの名前 */
  protected static final String DEFAULT_TEMPLATE_DIR_NAME =
      "org/seasar/doma/extension/gen/template";

  /** FreeMarkerの設定 */
  protected final Configuration configuration;

  /** インスタンスを構築します。 */
  protected Generator() {
    this("UTF-8", null, "java");
  }

  /**
   * インスタンスを構築します。
   *
   * @param templateEncoding テンプレートファイルのエンコーディング
   * @param templatePrimaryDir テンプレートファイルを格納したプライマリディレクトリ、プライマリディレクトリを使用しない場合{@code null}
   */
  public Generator(String templateEncoding, File templatePrimaryDir, String targetLanguage) {
    if (templateEncoding == null) {
      throw new NullPointerException("templateFileEncoding");
    }
    this.configuration = new Configuration();
    configuration.setObjectWrapper(new DefaultObjectWrapper());
    configuration.setSharedVariable("currentDate", new OnDemandDateModel());
    configuration.setEncoding(Locale.getDefault(), templateEncoding);
    configuration.setNumberFormat("0.#####");
    configuration.setTemplateLoader(createTemplateLoader(templatePrimaryDir, targetLanguage));
  }

  /**
   * {@link TemplateLoader}を作成します。
   *
   * @param templateFilePrimaryDir テンプレートファイルを格納したプライマリディレクトリ、プライマリディレクトリを使用しない場合{@code null}
   * @return {@link TemplateLoader}
   */
  protected TemplateLoader createTemplateLoader(File templateFilePrimaryDir, String targetLanguage) {
    TemplateLoader primary = null;
    if (templateFilePrimaryDir != null) {
      try {
        primary = new FileTemplateLoader(templateFilePrimaryDir);
      } catch (IOException e) {
        throw new GenException(Message.DOMAGEN9001, e, e);
      }
    }
    TemplateLoader secondary = new ResourceTemplateLoader(DEFAULT_TEMPLATE_DIR_NAME + "/" + targetLanguage);
    if (primary == null) {
      return secondary;
    }
    return new MultiTemplateLoader(new TemplateLoader[] {primary, secondary});
  }

  /**
   * 生成します。
   *
   * @param context コンテキスト
   */
  public void generate(GenerationContext context) {
    boolean exists = exists(context.getFile());
    if (!context.isOverwrite() && exists) {
      return;
    }
    File dir = context.getFile().getParentFile();
    if (dir != null) {
      mkdirs(dir);
    }
    Writer writer = openWriter(context);
    try {
      Template template = getTemplate(context.getTemplateName());
      process(template, context.getModel(), writer);
    } finally {
      IOUtil.close(writer);
    }
    if (exists) {
      Logger.info(Message.DOMAGEN0020.getMessage(context.getFile().getPath()));
    } else {
      Logger.info(Message.DOMAGEN0019.getMessage(context.getFile().getPath()));
    }
  }

  /**
   * {@code file}が存在する場合に{@code true}を返します。
   *
   * @param file ファイル
   * @return {@code file}が存在する場合は{@code true}、そうでない場合は{@code false}
   */
  protected boolean exists(File file) {
    return file.exists();
  }

  /**
   * ディレクトリを生成します。
   *
   * @param dir ディレクトリ
   */
  protected void mkdirs(File dir) {
    dir.mkdirs();
  }

  /**
   * {@link Writer}を開きます。
   *
   * @param context コンテキスト
   * @return {@link Writer}
   */
  protected Writer openWriter(GenerationContext context) {
    Charset charset = Charset.forName(context.getEncoding());
    return new BufferedWriter(
        new OutputStreamWriter(createFileOutputStream(context.getFile()), charset));
  }

  /**
   * ストリームを作成します。
   *
   * @param file ファイル
   * @return ストリーム
   */
  protected OutputStream createFileOutputStream(File file) {
    try {
      return new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      throw new GenException(Message.DOMAGEN9001, e, e);
    }
  }

  /**
   * テンプレートを取得します。
   *
   * @param name テンプレートの名前
   * @return テンプレート
   */
  protected Template getTemplate(String name) {
    try {
      return configuration.getTemplate(name);
    } catch (IOException e) {
      throw new GenException(Message.DOMAGEN9001, e, e);
    }
  }

  /**
   * テンプレートを処理します。
   *
   * @param template テンプレート
   * @param dataModel データモデル
   * @param writer ライタ
   */
  protected void process(Template template, Object dataModel, Writer writer) {
    try {
      template.process(dataModel, writer);
    } catch (IOException e) {
      throw new GenException(Message.DOMAGEN9001, e, e);
    } catch (TemplateException e) {
      throw new GenException(Message.DOMAGEN9001, e, e);
    }
  }
}
