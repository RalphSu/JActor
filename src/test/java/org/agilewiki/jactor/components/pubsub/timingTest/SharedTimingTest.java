package org.agilewiki.jactor.components.pubsub.timingTest;

import junit.framework.TestCase;
import org.agilewiki.jactor.*;
import org.agilewiki.jactor.bind.Open;
import org.agilewiki.jactor.components.Include;
import org.agilewiki.jactor.components.JCActor;
import org.agilewiki.jactor.components.pubsub.Subscribe;
import org.agilewiki.jactor.parallel.JAParallel;
import org.agilewiki.jactor.parallel.Run1Parallel;

/**
 * Test code.
 */
public class SharedTimingTest extends TestCase {
    public void test() {

        int c = 1;
        int s = 1;
        int p = 1;
        int t = 1;

        //int c = 10000;
        //int s = 1000;
        //int p = 4;
        //int t = 4;

        //4 parallel runs of 10000 requests sent to 1000 subscribers
        //publications per sec = 72202166
        //response time 55 nanoseconds

        //int c = 10000;
        //int s = 1000;
        //int p = 8;
        //int t = 4;

        //8 parallel runs of 10000 requests sent to 1000 subscribers
        //publications per sec = 81799591
        //response time 49 nanoseconds

        //int c = 1000000;
        //int s = 10;
        //int p = 8;
        //int t = 4;

        //8 parallel runs of 1000000 requests sent to 10 subscribers
        //publications per sec = 46457607
        //response time 86 nanoseconds

        //int c = 10000;
        //int s = 1000;
        //int p = 16;
        //int t = 4;

        //16 parallel runs of 10000 requests sent to 1000 subscribers
        //publications per sec = 75721722
        //response time 53 nanoseconds

        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(t);
        try {
            JAFuture future = new JAFuture();
            JCActor[] drivers = new JCActor[p];
            int i = 0;
            while (i < p) {
                Mailbox sharedMailbox = mailboxFactory.createAsyncMailbox();
                JCActor driver = new JCActor(sharedMailbox);
                (new Include(Driver1.class)).call(driver);
                Open.req.call(driver);
                drivers[i] = driver;
                int j = 0;
                while (j < s) {
                    Actor subscriber = new NullSubscriber(sharedMailbox);
                    Open.req.call(subscriber);
                    (new Subscribe(subscriber)).call(driver);
                    j += 1;
                }
                i += 1;
            }
            JAParallel parallel = new JAParallel(mailboxFactory.createMailbox(), drivers);
            Timing timing = new Timing(c, 1);
            Run1Parallel run1Parallel = new Run1Parallel(timing);
            run1Parallel.send(future, parallel);
            run1Parallel.send(future, parallel);
            long t0 = System.currentTimeMillis();
            run1Parallel.send(future, parallel);
            long t1 = System.currentTimeMillis();
            System.out.println("" + p + " parallel runs of " + c + " requests sent to " + s + " subscribers");
            if (t1 != t0)
                System.out.println("publications per sec = " + ((c * s * p) * 1000L / (t1 - t0)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mailboxFactory.close();
        }
    }
}
