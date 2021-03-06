/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.orchestrator.services;

import org.openscore.engine.node.entities.WorkerNode;
import org.openscore.engine.node.services.WorkerLockService;
import org.openscore.engine.node.services.WorkerNodeService;
import org.openscore.engine.queue.entities.ExecutionMessage;
import org.openscore.engine.queue.services.QueueDispatcherService;
import org.openscore.orchestrator.entities.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 24/07/14
 * Time: 08:58
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class OrchestratorDispatcherServiceTest {

    @InjectMocks
    private OrchestratorDispatcherService orchestratorDispatcherService = new OrchestratorDispatcherServiceImpl();

    @Mock
    WorkerNodeService workerNodeService;

    @Mock
    WorkerLockService workerLockService;

    @Mock
    private QueueDispatcherService queueDispatcher;

    @Mock
    private SplitJoinService splitJoinService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void simpleDispatchTest(){
        List<Message> messages = new ArrayList<>();
        messages.add(new ExecutionMessage());

        String newBulkNumber = "2";
        String uuid = "123";

        WorkerNode node = new WorkerNode();
        node.setBulkNumber("1");
        node.setWorkerRecoveryVersion("1");

        when(workerNodeService.readByUUID(anyString())).thenReturn(node);

        orchestratorDispatcherService.dispatch(messages, newBulkNumber, "1", uuid);
        Mockito.verify(workerLockService, times(1)).lock(uuid);
        Mockito.verify(queueDispatcher, times(1)).dispatch(anyList());
        Mockito.verify(workerNodeService, times(1)).updateBulkNumber(uuid, newBulkNumber);
    }

    @Test
    public void firstDispatchTest(){
        List<Message> messages = new ArrayList<>();
        messages.add(new ExecutionMessage());

        String newBulkNumber = "2";
        String uuid = "123";

        WorkerNode node = new WorkerNode(); //node with NULL bulk number since it is first dispatch
        node.setWorkerRecoveryVersion("1");

        when(workerNodeService.readByUUID(anyString())).thenReturn(node);

        orchestratorDispatcherService.dispatch(messages, newBulkNumber, "1", uuid);
        Mockito.verify(workerLockService, times(1)).lock(uuid);
        Mockito.verify(queueDispatcher, times(1)).dispatch(anyList());
        Mockito.verify(workerNodeService, times(1)).updateBulkNumber(uuid, newBulkNumber);
    }

    @Test
    public void sameBulkDispatchTest(){
        List<Message> messages = new ArrayList<>();
        messages.add(new ExecutionMessage());

        String newBulkNumber = "1";
        String uuid = "123";

        WorkerNode node = new WorkerNode();
        node.setBulkNumber("1");
        node.setWorkerRecoveryVersion("1");

        when(workerNodeService.readByUUID(anyString())).thenReturn(node);

        orchestratorDispatcherService.dispatch(messages, newBulkNumber, "1", uuid);
        Mockito.verify(workerLockService, times(1)).lock(uuid);
        Mockito.verify(queueDispatcher, times(0)).dispatch(anyList());
        Mockito.verify(workerNodeService, times(0)).updateBulkNumber(uuid, newBulkNumber);
    }
    
    @Test
        public void dispatchAfterRecoveryTest(){
            List<Message> messages = new ArrayList<>();
            messages.add(new ExecutionMessage());
    
            String newBulkNumber = "2";
            String uuid = "123";
    
            WorkerNode node = new WorkerNode();
            node.setBulkNumber("1");
            node.setWorkerRecoveryVersion("1");
    
            when(workerNodeService.readByUUID(anyString())).thenReturn(node);
    
            //worker dispatches with wrong WRV
            orchestratorDispatcherService.dispatch(messages, newBulkNumber, "0", uuid);
    
            Mockito.verify(queueDispatcher, times(0)).dispatch(anyList());
            Mockito.verify(workerNodeService, times(0)).updateBulkNumber(uuid, newBulkNumber);
        }

    @Configuration
    static class EmptyConfig {

    }

}
