/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.engine.node.services;

import com.google.common.collect.Multimap;
import org.eclipse.score.api.nodes.WorkerStatus;
import org.eclipse.score.engine.node.entities.WorkerNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 08/11/12
 */
//TODO: Add Javadoc Eliya
public interface WorkerNodeService {

    /**
     * Update the Worker Node entity with the current ack version for the keep alive mechanism
     * @param uuid worker's unique identifier
     * @return the worker's recovery version (WRV)
     */
	String keepAlive(String uuid);

    /**
     * Create a new worker
     * @param uuid  worker's unique identifier
     * @param password worker's password
     * @param hostName  worker's host
     * @param installDir worker's installation directory
     */
	void create(String uuid, String password, String hostName, String installDir);

    void updateWorkerToDeleted(String uuid);

    List<WorkerNode> readAllNotDeletedWorkers();

	String up(String uuid);

   // find not deleted worker by uuid
	WorkerNode readByUUID(String uuid);

    // is not relating to IS_DELETED property
    WorkerNode findByUuid(String uuid);

	List<WorkerNode> readAllWorkers();

	List<String> readNonRespondingWorkers();

	List<WorkerNode> readWorkersByActivation(boolean isActive);

	void activate(String uuid);

	void deactivate(String uuid);

	void updateEnvironmentParams(String uuid, String os, String jvm, String dotNetVersion);

	void updateStatus(String uuid, WorkerStatus status);

    void updateStatusInSeparateTransaction(String uuid, WorkerStatus status);

    List<String> readAllWorkerGroups();

    List<String> readWorkerGroups(String uuid);

	void updateWorkerGroups(String uuid, String... groupNames);

	Multimap<String, String> readGroupWorkersMapActiveAndRunning();

	void addGroupToWorker(String workerUuid, String group);

	void removeGroupFromWorker(String workerUuid, String group);

	List<String> readWorkerGroups(List<String> groups);

    void updateBulkNumber(String workerUuid, String bulkNumber);

    void updateWRV(String workerUuid, String wrv);

    List<String> readAllWorkersUuids();
}