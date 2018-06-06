package a.b.c.d.tslrm.log;

import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DefaultFileAppender extends FileAppender {
    static PatternLayout defaultLayout = new PatternLayout("%d %p [%c %L] - <%m>%n");

    public DefaultFileAppender(String fileName) throws IOException {
        super(defaultLayout,fileName);
    }

    static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyyMMdd HH_mm_ss");

    public DefaultFileAppender(Class clazz) throws IOException {
        this("log" + "//" + clazz.getSimpleName() + "-" + defaultDateFormat.format(new Date()) + ".txt");
    }

}
