/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.impl.history.async.json.transformer;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.impl.history.async.HistoryJsonConstants;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.flowable.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.flowable.engine.impl.persistence.entity.HistoricTaskInstanceEntityManager;
import org.flowable.engine.impl.persistence.entity.JobEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class TaskCreatedHistoryJsonTransformer extends AbstractNeedsUnfinishedHistoricActivityHistoryJsonTransformer {

    public static final String TYPE = "task-created";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void transformJson(JobEntity job, ObjectNode historicalData, CommandContext commandContext) {
        HistoricTaskInstanceEntityManager historicTaskInstanceEntityManager = commandContext.getHistoricTaskInstanceEntityManager();
        HistoricTaskInstanceEntity historicTaskInstance = historicTaskInstanceEntityManager.create();

        String taskId = getStringFromJson(historicalData, HistoryJsonConstants.ID);
        historicTaskInstance.setId(taskId);
        historicTaskInstance.setProcessDefinitionId(getStringFromJson(historicalData, HistoryJsonConstants.PROCESS_DEFINITION_ID));
        historicTaskInstance.setProcessInstanceId(getStringFromJson(historicalData, HistoryJsonConstants.PROCESS_INSTANCE_ID));
        String executionId = getStringFromJson(historicalData, HistoryJsonConstants.EXECUTION_ID);
        historicTaskInstance.setExecutionId(executionId);
        historicTaskInstance.setName(getStringFromJson(historicalData, HistoryJsonConstants.NAME));
        historicTaskInstance.setParentTaskId(getStringFromJson(historicalData, HistoryJsonConstants.PARENT_TASK_ID));
        historicTaskInstance.setDescription(getStringFromJson(historicalData, HistoryJsonConstants.DESCRIPTION));
        historicTaskInstance.setOwner(getStringFromJson(historicalData, HistoryJsonConstants.OWNER));
        historicTaskInstance.setAssignee(getStringFromJson(historicalData, HistoryJsonConstants.ASSIGNEE));
        historicTaskInstance.setStartTime(getDateFromJson(historicalData, HistoryJsonConstants.START_TIME));
        historicTaskInstance.setTaskDefinitionKey(getStringFromJson(historicalData, HistoryJsonConstants.TASK_DEFINITION_KEY));
        historicTaskInstance.setPriority(getIntegerFromJson(historicalData, HistoryJsonConstants.PRIORITY));
        historicTaskInstance.setDueDate(getDateFromJson(historicalData, HistoryJsonConstants.DUE_DATE));
        historicTaskInstance.setCategory(getStringFromJson(historicalData, HistoryJsonConstants.CATEGORY));
        historicTaskInstance.setTenantId(getStringFromJson(historicalData, getStringFromJson(historicalData, HistoryJsonConstants.TENANT_ID)));

        historicTaskInstanceEntityManager.insert(historicTaskInstance);

        if (StringUtils.isNotEmpty(executionId)) {
            String activityId = getStringFromJson(historicalData, HistoryJsonConstants.ACTIVITY_ID);
            if (StringUtils.isNotEmpty(activityId)) {
                HistoricActivityInstanceEntity historicActivityInstanceEntity = findUnfinishedHistoricActivityInstance(commandContext, executionId, activityId);
                if (historicActivityInstanceEntity != null) {
                    historicActivityInstanceEntity.setTaskId(taskId);
                }
            }
        }
    }

}