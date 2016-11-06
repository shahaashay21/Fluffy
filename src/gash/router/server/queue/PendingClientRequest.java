package gash.router.server.queue;

import com.google.protobuf.GeneratedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by n on 4/7/16.
 */
public class PendingClientRequest {

    protected static Logger logger = LoggerFactory.getLogger("PendingClientRequest");

    LinkedBlockingDeque<GeneratedMessage> waitingForResponse;


}
