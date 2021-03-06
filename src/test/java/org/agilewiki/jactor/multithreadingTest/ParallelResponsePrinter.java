package org.agilewiki.jactor.multithreadingTest;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.bind.Internals;
import org.agilewiki.jactor.bind.MethodBinding;
import org.agilewiki.jactor.components.Component;
import org.agilewiki.jactor.components.JCActor;
import org.agilewiki.jactor.components.pubsub.PubSubResponseProcessor;

/**
 * Test code.
 */
public class ParallelResponsePrinter extends Component {
    @Override
    public void bindery() throws Exception {

        thisActor.bind(
                PrintParallelResponse.class.getName(),
                new MethodBinding<PrintParallelResponse<Object>, Object>() {
                    @Override
                    public void processRequest(Internals internals,
                                               PrintParallelResponse request,
                                               RP rp)
                            throws Exception {
                        int count = request.getCount();
                        JCActor[] responsePrinters = request.getResponsePrinters();
                        PrintResponse printResponse = request.getPrintResponse();
                        PubSubResponseProcessor psrp = new PubSubResponseProcessor(rp);
                        int i = 0;
                        while (i < count) {
                            System.out.println(i);
                            printResponse.send(internals, responsePrinters[i], psrp);
                            i += 1;
                        }
                        psrp.sent = count;
                        psrp.finished();
                        System.out.println(count + " sends");
                    }
                });

    }
}
