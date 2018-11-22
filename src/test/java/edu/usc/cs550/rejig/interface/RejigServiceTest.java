package edu.usc.cs550.rejig.interfaces;

import static org.junit.Assert.assertEquals;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.io.IOException;
import java.util.logging.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class RejigServiceTest {
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  /**
   * Test a simple, dummpy implementation of the RejigReader service.
   */
  @Test
  public void rejigReader_getConfig() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    // Create a server, add service, start, and register for automatic
    // graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
      .forName(serverName).directExecutor()
      .addService(new RejigReaderTestImpl())
      .build().start());

    RejigReaderGrpc.RejigReaderBlockingStub blockingStub = RejigReaderGrpc
      .newBlockingStub(grpcCleanup
        .register(InProcessChannelBuilder.forName(serverName)
          .directExecutor().build()));

    RejigConfig reply = blockingStub.getConfig(Empty.getDefaultInstance());

    assertEquals(reply.getId(), 1);
    assertEquals(reply.getFragmentCount(), 4);
  }

  /**
   * Test a simple, dummpy implementation of the RejigWriter service.
   */
  @Test
  public void rejigWriter_setConfig() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    // Create a server, add service, start, and register for automatic
    // graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
      .forName(serverName).directExecutor()
      .addService(new RejigWriterTestImpl())
      .build().start());

    RejigWriterGrpc.RejigWriterBlockingStub blockingStub = RejigWriterGrpc
      .newBlockingStub(grpcCleanup
        .register(InProcessChannelBuilder.forName(serverName)
          .directExecutor().build()));

    FragmentList fragments = FragmentList.newBuilder()
        .addFragment(Fragment.newBuilder().setAddress("server1:port_a"))
        .addFragment(Fragment.newBuilder().setAddress("server1:port_a"))
        .build();
    RejigConfig reply = blockingStub.setConfig(fragments);

    assertEquals(reply.getId(), 2);
    assertEquals(reply.getFragmentCount(), 2);
  }
}

/**
 * A dummy implementation of the RejigReader service.
 */
class RejigReaderTestImpl extends RejigReaderGrpc.RejigReaderImplBase {
  @Override
  public void getConfig(Empty req, StreamObserver<RejigConfig> responseObserver) {
    RejigConfig config = RejigConfig.newBuilder()
      .setId(1)
      .addFragment(Fragment.newBuilder().setAddress("server1:port_a"))
      .addFragment(Fragment.newBuilder().setAddress("server2:port_a"))
      .addFragment(Fragment.newBuilder().setAddress("server1:port_b"))
      .addFragment(Fragment.newBuilder().setAddress("server3:port_b"))
      .build();
    responseObserver.onNext(config);
    responseObserver.onCompleted();
  }
}

/**
 * A dummy implementation of the RejigWriter service.
 */
class RejigWriterTestImpl extends RejigWriterGrpc.RejigWriterImplBase {
  @Override
  public void setConfig(FragmentList req, StreamObserver<RejigConfig> responseObserver) {
    RejigConfig config = RejigConfig.newBuilder()
      .setId(2)
      .addAllFragment(req.getFragmentList())
      .build();
    responseObserver.onNext(config);
    responseObserver.onCompleted();
  }
}
