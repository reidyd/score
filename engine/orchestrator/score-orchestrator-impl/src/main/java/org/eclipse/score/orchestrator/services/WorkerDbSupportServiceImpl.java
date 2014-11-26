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
package org.eclipse.score.orchestrator.services;

import org.eclipse.score.facade.entities.RunningExecutionPlan;
import org.eclipse.score.facade.services.RunningExecutionPlanService;
import org.eclipse.score.worker.management.services.dbsupport.WorkerDbSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 13:38
 */
public class WorkerDbSupportServiceImpl implements WorkerDbSupportService {

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    @Override
    @Cacheable("running_execution_plans")
    public RunningExecutionPlan readExecutionPlanById(Long runningExecutionPlanId) {
        return runningExecutionPlanService.readExecutionPlanById(runningExecutionPlanId);
    }
}