package a.b.c.d.tslrm;

import a.b.c.d.tslrm.log.DefaultFileAppender;
import junit.framework.Assert;
import org.apache.log4j.*;
import org.junit.Test;

import java.io.IOException;


public class TestLog4j {
    static Logger logger = Logger.getLogger(TestLog4j.class);

    @Test
    public void test() throws IOException {




        Logger.getRootLogger().addAppender(new DefaultFileAppender(this.getClass()));
        logger.debug("debug");
        logger.error("error");
    }

    @Test
    public void testAdd()
    {
        int i = 1;
        int j = 3;
        Assert.assertEquals(4.2,1+3);
    }


}
