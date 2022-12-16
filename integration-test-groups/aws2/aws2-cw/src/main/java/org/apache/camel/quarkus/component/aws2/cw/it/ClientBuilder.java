/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.component.aws2.cw.it;

import java.util.function.Consumer;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.CamelContext;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.ConcurrentModificationException;
import software.amazon.awssdk.services.cloudwatch.model.DashboardInvalidInputErrorException;
import software.amazon.awssdk.services.cloudwatch.model.DashboardNotFoundErrorException;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsResponse;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAnomalyDetectorRequest;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatch.model.DeleteDashboardsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DeleteDashboardsResponse;
import software.amazon.awssdk.services.cloudwatch.model.DeleteInsightRulesRequest;
import software.amazon.awssdk.services.cloudwatch.model.DeleteInsightRulesResponse;
import software.amazon.awssdk.services.cloudwatch.model.DeleteMetricStreamRequest;
import software.amazon.awssdk.services.cloudwatch.model.DeleteMetricStreamResponse;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmHistoryRequest;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmHistoryResponse;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsForMetricRequest;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsForMetricResponse;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsResponse;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAnomalyDetectorsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAnomalyDetectorsResponse;
import software.amazon.awssdk.services.cloudwatch.model.DescribeInsightRulesRequest;
import software.amazon.awssdk.services.cloudwatch.model.DescribeInsightRulesResponse;
import software.amazon.awssdk.services.cloudwatch.model.DisableAlarmActionsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DisableAlarmActionsResponse;
import software.amazon.awssdk.services.cloudwatch.model.DisableInsightRulesRequest;
import software.amazon.awssdk.services.cloudwatch.model.DisableInsightRulesResponse;
import software.amazon.awssdk.services.cloudwatch.model.EnableAlarmActionsRequest;
import software.amazon.awssdk.services.cloudwatch.model.EnableAlarmActionsResponse;
import software.amazon.awssdk.services.cloudwatch.model.EnableInsightRulesRequest;
import software.amazon.awssdk.services.cloudwatch.model.EnableInsightRulesResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetDashboardRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetDashboardResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetInsightRuleReportRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetInsightRuleReportResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStreamRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStreamResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricWidgetImageRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricWidgetImageResponse;
import software.amazon.awssdk.services.cloudwatch.model.InternalServiceException;
import software.amazon.awssdk.services.cloudwatch.model.InvalidFormatException;
import software.amazon.awssdk.services.cloudwatch.model.InvalidNextTokenException;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterCombinationException;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.cloudwatch.model.LimitExceededException;
import software.amazon.awssdk.services.cloudwatch.model.ListDashboardsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListDashboardsResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListManagedInsightRulesRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListManagedInsightRulesResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricStreamsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricStreamsResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.cloudwatch.model.MissingRequiredParameterException;
import software.amazon.awssdk.services.cloudwatch.model.PutAnomalyDetectorRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutCompositeAlarmRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutCompositeAlarmResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutDashboardRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutDashboardResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutInsightRuleRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutInsightRuleResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutManagedInsightRulesRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutManagedInsightRulesResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricStreamRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricStreamResponse;
import software.amazon.awssdk.services.cloudwatch.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatch.model.SetAlarmStateRequest;
import software.amazon.awssdk.services.cloudwatch.model.SetAlarmStateResponse;
import software.amazon.awssdk.services.cloudwatch.model.StartMetricStreamsRequest;
import software.amazon.awssdk.services.cloudwatch.model.StartMetricStreamsResponse;
import software.amazon.awssdk.services.cloudwatch.model.StopMetricStreamsRequest;
import software.amazon.awssdk.services.cloudwatch.model.StopMetricStreamsResponse;
import software.amazon.awssdk.services.cloudwatch.model.TagResourceRequest;
import software.amazon.awssdk.services.cloudwatch.model.TagResourceResponse;
import software.amazon.awssdk.services.cloudwatch.model.UntagResourceRequest;
import software.amazon.awssdk.services.cloudwatch.model.UntagResourceResponse;
import software.amazon.awssdk.services.cloudwatch.paginators.DescribeAlarmHistoryIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.DescribeAlarmsIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.DescribeAnomalyDetectorsIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.DescribeInsightRulesIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.GetMetricDataIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.ListDashboardsIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.ListManagedInsightRulesIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.ListMetricStreamsIterable;
import software.amazon.awssdk.services.cloudwatch.paginators.ListMetricsIterable;
import software.amazon.awssdk.services.cloudwatch.waiters.CloudWatchWaiter;

public class ClientBuilder {

    @Inject
    CamelContext camelContext;

    @Produces
    @Named("customClient")
    CloudWatchClient produceClient() {

        return new MockedClient();
    }

    @Produces
    @Named("customClient2")
    CloudWatchClient produceClient2() {
        //second client is required to avoid autowiring of the first client if there is only one client
        return new MockedClient();
    }

    class MockedClient implements CloudWatchClient {

        @Override
        public PutMetricDataResponse putMetricData(PutMetricDataRequest putMetricDataRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidParameterCombinationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            throw new RuntimeException(this.getClass().getSimpleName());
        }

        //--------------------- generated by IDE --------------------------

        @Override
        public String serviceName() {
            return null;
        }

        @Override
        public void close() {

        }

        @Override
        public DeleteAlarmsResponse deleteAlarms(DeleteAlarmsRequest deleteAlarmsRequest)
                throws ResourceNotFoundException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteAlarms(deleteAlarmsRequest);
        }

        @Override
        public DeleteAlarmsResponse deleteAlarms(Consumer<DeleteAlarmsRequest.Builder> deleteAlarmsRequest)
                throws ResourceNotFoundException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteAlarms(deleteAlarmsRequest);
        }

        @Override
        public DeleteAnomalyDetectorResponse deleteAnomalyDetector(DeleteAnomalyDetectorRequest deleteAnomalyDetectorRequest)
                throws ResourceNotFoundException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, InvalidParameterCombinationException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteAnomalyDetector(deleteAnomalyDetectorRequest);
        }

        @Override
        public DeleteAnomalyDetectorResponse deleteAnomalyDetector(
                Consumer<DeleteAnomalyDetectorRequest.Builder> deleteAnomalyDetectorRequest) throws ResourceNotFoundException,
                InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                InvalidParameterCombinationException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteAnomalyDetector(deleteAnomalyDetectorRequest);
        }

        @Override
        public DeleteDashboardsResponse deleteDashboards(DeleteDashboardsRequest deleteDashboardsRequest)
                throws InvalidParameterValueException, DashboardNotFoundErrorException, InternalServiceException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteDashboards(deleteDashboardsRequest);
        }

        @Override
        public DeleteDashboardsResponse deleteDashboards(Consumer<DeleteDashboardsRequest.Builder> deleteDashboardsRequest)
                throws InvalidParameterValueException, DashboardNotFoundErrorException, InternalServiceException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteDashboards(deleteDashboardsRequest);
        }

        @Override
        public DeleteInsightRulesResponse deleteInsightRules(DeleteInsightRulesRequest deleteInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteInsightRules(deleteInsightRulesRequest);
        }

        @Override
        public DeleteInsightRulesResponse deleteInsightRules(
                Consumer<DeleteInsightRulesRequest.Builder> deleteInsightRulesRequest) throws InvalidParameterValueException,
                MissingRequiredParameterException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteInsightRules(deleteInsightRulesRequest);
        }

        @Override
        public DeleteMetricStreamResponse deleteMetricStream(DeleteMetricStreamRequest deleteMetricStreamRequest)
                throws InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteMetricStream(deleteMetricStreamRequest);
        }

        @Override
        public DeleteMetricStreamResponse deleteMetricStream(
                Consumer<DeleteMetricStreamRequest.Builder> deleteMetricStreamRequest)
                throws InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.deleteMetricStream(deleteMetricStreamRequest);
        }

        @Override
        public DescribeAlarmHistoryResponse describeAlarmHistory()
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmHistory();
        }

        @Override
        public DescribeAlarmHistoryResponse describeAlarmHistory(DescribeAlarmHistoryRequest describeAlarmHistoryRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmHistory(describeAlarmHistoryRequest);
        }

        @Override
        public DescribeAlarmHistoryResponse describeAlarmHistory(
                Consumer<DescribeAlarmHistoryRequest.Builder> describeAlarmHistoryRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmHistory(describeAlarmHistoryRequest);
        }

        @Override
        public DescribeAlarmHistoryIterable describeAlarmHistoryPaginator()
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmHistoryPaginator();
        }

        @Override
        public DescribeAlarmHistoryIterable describeAlarmHistoryPaginator(
                DescribeAlarmHistoryRequest describeAlarmHistoryRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmHistoryPaginator(describeAlarmHistoryRequest);
        }

        @Override
        public DescribeAlarmHistoryIterable describeAlarmHistoryPaginator(
                Consumer<DescribeAlarmHistoryRequest.Builder> describeAlarmHistoryRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmHistoryPaginator(describeAlarmHistoryRequest);
        }

        @Override
        public DescribeAlarmsResponse describeAlarms()
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarms();
        }

        @Override
        public DescribeAlarmsResponse describeAlarms(DescribeAlarmsRequest describeAlarmsRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarms(describeAlarmsRequest);
        }

        @Override
        public DescribeAlarmsResponse describeAlarms(Consumer<DescribeAlarmsRequest.Builder> describeAlarmsRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarms(describeAlarmsRequest);
        }

        @Override
        public DescribeAlarmsIterable describeAlarmsPaginator()
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmsPaginator();
        }

        @Override
        public DescribeAlarmsIterable describeAlarmsPaginator(DescribeAlarmsRequest describeAlarmsRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmsPaginator(describeAlarmsRequest);
        }

        @Override
        public DescribeAlarmsIterable describeAlarmsPaginator(Consumer<DescribeAlarmsRequest.Builder> describeAlarmsRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmsPaginator(describeAlarmsRequest);
        }

        @Override
        public DescribeAlarmsForMetricResponse describeAlarmsForMetric(
                DescribeAlarmsForMetricRequest describeAlarmsForMetricRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmsForMetric(describeAlarmsForMetricRequest);
        }

        @Override
        public DescribeAlarmsForMetricResponse describeAlarmsForMetric(
                Consumer<DescribeAlarmsForMetricRequest.Builder> describeAlarmsForMetricRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAlarmsForMetric(describeAlarmsForMetricRequest);
        }

        @Override
        public DescribeAnomalyDetectorsResponse describeAnomalyDetectors(
                DescribeAnomalyDetectorsRequest describeAnomalyDetectorsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                InvalidParameterCombinationException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAnomalyDetectors(describeAnomalyDetectorsRequest);
        }

        @Override
        public DescribeAnomalyDetectorsResponse describeAnomalyDetectors(
                Consumer<DescribeAnomalyDetectorsRequest.Builder> describeAnomalyDetectorsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                InvalidParameterCombinationException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAnomalyDetectors(describeAnomalyDetectorsRequest);
        }

        @Override
        public DescribeAnomalyDetectorsIterable describeAnomalyDetectorsPaginator(
                DescribeAnomalyDetectorsRequest describeAnomalyDetectorsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                InvalidParameterCombinationException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAnomalyDetectorsPaginator(describeAnomalyDetectorsRequest);
        }

        @Override
        public DescribeAnomalyDetectorsIterable describeAnomalyDetectorsPaginator(
                Consumer<DescribeAnomalyDetectorsRequest.Builder> describeAnomalyDetectorsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                InvalidParameterCombinationException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeAnomalyDetectorsPaginator(describeAnomalyDetectorsRequest);
        }

        @Override
        public DescribeInsightRulesResponse describeInsightRules(DescribeInsightRulesRequest describeInsightRulesRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeInsightRules(describeInsightRulesRequest);
        }

        @Override
        public DescribeInsightRulesResponse describeInsightRules(
                Consumer<DescribeInsightRulesRequest.Builder> describeInsightRulesRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeInsightRules(describeInsightRulesRequest);
        }

        @Override
        public DescribeInsightRulesIterable describeInsightRulesPaginator(
                DescribeInsightRulesRequest describeInsightRulesRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeInsightRulesPaginator(describeInsightRulesRequest);
        }

        @Override
        public DescribeInsightRulesIterable describeInsightRulesPaginator(
                Consumer<DescribeInsightRulesRequest.Builder> describeInsightRulesRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.describeInsightRulesPaginator(describeInsightRulesRequest);
        }

        @Override
        public DisableAlarmActionsResponse disableAlarmActions(DisableAlarmActionsRequest disableAlarmActionsRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.disableAlarmActions(disableAlarmActionsRequest);
        }

        @Override
        public DisableAlarmActionsResponse disableAlarmActions(
                Consumer<DisableAlarmActionsRequest.Builder> disableAlarmActionsRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.disableAlarmActions(disableAlarmActionsRequest);
        }

        @Override
        public DisableInsightRulesResponse disableInsightRules(DisableInsightRulesRequest disableInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.disableInsightRules(disableInsightRulesRequest);
        }

        @Override
        public DisableInsightRulesResponse disableInsightRules(
                Consumer<DisableInsightRulesRequest.Builder> disableInsightRulesRequest) throws InvalidParameterValueException,
                MissingRequiredParameterException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.disableInsightRules(disableInsightRulesRequest);
        }

        @Override
        public EnableAlarmActionsResponse enableAlarmActions(EnableAlarmActionsRequest enableAlarmActionsRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.enableAlarmActions(enableAlarmActionsRequest);
        }

        @Override
        public EnableAlarmActionsResponse enableAlarmActions(
                Consumer<EnableAlarmActionsRequest.Builder> enableAlarmActionsRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.enableAlarmActions(enableAlarmActionsRequest);
        }

        @Override
        public EnableInsightRulesResponse enableInsightRules(EnableInsightRulesRequest enableInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, LimitExceededException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.enableInsightRules(enableInsightRulesRequest);
        }

        @Override
        public EnableInsightRulesResponse enableInsightRules(
                Consumer<EnableInsightRulesRequest.Builder> enableInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, LimitExceededException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.enableInsightRules(enableInsightRulesRequest);
        }

        @Override
        public GetDashboardResponse getDashboard(GetDashboardRequest getDashboardRequest)
                throws InvalidParameterValueException, DashboardNotFoundErrorException, InternalServiceException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getDashboard(getDashboardRequest);
        }

        @Override
        public GetDashboardResponse getDashboard(Consumer<GetDashboardRequest.Builder> getDashboardRequest)
                throws InvalidParameterValueException, DashboardNotFoundErrorException, InternalServiceException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getDashboard(getDashboardRequest);
        }

        @Override
        public GetInsightRuleReportResponse getInsightRuleReport(GetInsightRuleReportRequest getInsightRuleReportRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, ResourceNotFoundException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getInsightRuleReport(getInsightRuleReportRequest);
        }

        @Override
        public GetInsightRuleReportResponse getInsightRuleReport(
                Consumer<GetInsightRuleReportRequest.Builder> getInsightRuleReportRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, ResourceNotFoundException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getInsightRuleReport(getInsightRuleReportRequest);
        }

        @Override
        public GetMetricDataResponse getMetricData(GetMetricDataRequest getMetricDataRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricData(getMetricDataRequest);
        }

        @Override
        public GetMetricDataResponse getMetricData(Consumer<GetMetricDataRequest.Builder> getMetricDataRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricData(getMetricDataRequest);
        }

        @Override
        public GetMetricDataIterable getMetricDataPaginator(GetMetricDataRequest getMetricDataRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricDataPaginator(getMetricDataRequest);
        }

        @Override
        public GetMetricDataIterable getMetricDataPaginator(Consumer<GetMetricDataRequest.Builder> getMetricDataRequest)
                throws InvalidNextTokenException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricDataPaginator(getMetricDataRequest);
        }

        @Override
        public GetMetricStatisticsResponse getMetricStatistics(GetMetricStatisticsRequest getMetricStatisticsRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidParameterCombinationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricStatistics(getMetricStatisticsRequest);
        }

        @Override
        public GetMetricStatisticsResponse getMetricStatistics(
                Consumer<GetMetricStatisticsRequest.Builder> getMetricStatisticsRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidParameterCombinationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricStatistics(getMetricStatisticsRequest);
        }

        @Override
        public GetMetricStreamResponse getMetricStream(GetMetricStreamRequest getMetricStreamRequest)
                throws ResourceNotFoundException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, InvalidParameterCombinationException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricStream(getMetricStreamRequest);
        }

        @Override
        public GetMetricStreamResponse getMetricStream(Consumer<GetMetricStreamRequest.Builder> getMetricStreamRequest)
                throws ResourceNotFoundException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, InvalidParameterCombinationException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricStream(getMetricStreamRequest);
        }

        @Override
        public GetMetricWidgetImageResponse getMetricWidgetImage(GetMetricWidgetImageRequest getMetricWidgetImageRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricWidgetImage(getMetricWidgetImageRequest);
        }

        @Override
        public GetMetricWidgetImageResponse getMetricWidgetImage(
                Consumer<GetMetricWidgetImageRequest.Builder> getMetricWidgetImageRequest)
                throws AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.getMetricWidgetImage(getMetricWidgetImageRequest);
        }

        @Override
        public ListDashboardsResponse listDashboards() throws InvalidParameterValueException, InternalServiceException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listDashboards();
        }

        @Override
        public ListDashboardsResponse listDashboards(ListDashboardsRequest listDashboardsRequest)
                throws InvalidParameterValueException, InternalServiceException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.listDashboards(listDashboardsRequest);
        }

        @Override
        public ListDashboardsResponse listDashboards(Consumer<ListDashboardsRequest.Builder> listDashboardsRequest)
                throws InvalidParameterValueException, InternalServiceException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.listDashboards(listDashboardsRequest);
        }

        @Override
        public ListDashboardsIterable listDashboardsPaginator() throws InvalidParameterValueException, InternalServiceException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listDashboardsPaginator();
        }

        @Override
        public ListDashboardsIterable listDashboardsPaginator(ListDashboardsRequest listDashboardsRequest)
                throws InvalidParameterValueException, InternalServiceException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.listDashboardsPaginator(listDashboardsRequest);
        }

        @Override
        public ListDashboardsIterable listDashboardsPaginator(Consumer<ListDashboardsRequest.Builder> listDashboardsRequest)
                throws InvalidParameterValueException, InternalServiceException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.listDashboardsPaginator(listDashboardsRequest);
        }

        @Override
        public ListManagedInsightRulesResponse listManagedInsightRules(
                ListManagedInsightRulesRequest listManagedInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidNextTokenException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listManagedInsightRules(listManagedInsightRulesRequest);
        }

        @Override
        public ListManagedInsightRulesResponse listManagedInsightRules(
                Consumer<ListManagedInsightRulesRequest.Builder> listManagedInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidNextTokenException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listManagedInsightRules(listManagedInsightRulesRequest);
        }

        @Override
        public ListManagedInsightRulesIterable listManagedInsightRulesPaginator(
                ListManagedInsightRulesRequest listManagedInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidNextTokenException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listManagedInsightRulesPaginator(listManagedInsightRulesRequest);
        }

        @Override
        public ListManagedInsightRulesIterable listManagedInsightRulesPaginator(
                Consumer<ListManagedInsightRulesRequest.Builder> listManagedInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidNextTokenException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listManagedInsightRulesPaginator(listManagedInsightRulesRequest);
        }

        @Override
        public ListMetricStreamsResponse listMetricStreams(ListMetricStreamsRequest listMetricStreamsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetricStreams(listMetricStreamsRequest);
        }

        @Override
        public ListMetricStreamsResponse listMetricStreams(Consumer<ListMetricStreamsRequest.Builder> listMetricStreamsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetricStreams(listMetricStreamsRequest);
        }

        @Override
        public ListMetricStreamsIterable listMetricStreamsPaginator(ListMetricStreamsRequest listMetricStreamsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetricStreamsPaginator(listMetricStreamsRequest);
        }

        @Override
        public ListMetricStreamsIterable listMetricStreamsPaginator(
                Consumer<ListMetricStreamsRequest.Builder> listMetricStreamsRequest)
                throws InvalidNextTokenException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetricStreamsPaginator(listMetricStreamsRequest);
        }

        @Override
        public ListMetricsResponse listMetrics() throws InternalServiceException, InvalidParameterValueException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetrics();
        }

        @Override
        public ListMetricsResponse listMetrics(ListMetricsRequest listMetricsRequest) throws InternalServiceException,
                InvalidParameterValueException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetrics(listMetricsRequest);
        }

        @Override
        public ListMetricsResponse listMetrics(Consumer<ListMetricsRequest.Builder> listMetricsRequest)
                throws InternalServiceException, InvalidParameterValueException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.listMetrics(listMetricsRequest);
        }

        @Override
        public ListMetricsIterable listMetricsPaginator() throws InternalServiceException, InvalidParameterValueException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetricsPaginator();
        }

        @Override
        public ListMetricsIterable listMetricsPaginator(ListMetricsRequest listMetricsRequest) throws InternalServiceException,
                InvalidParameterValueException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listMetricsPaginator(listMetricsRequest);
        }

        @Override
        public ListMetricsIterable listMetricsPaginator(Consumer<ListMetricsRequest.Builder> listMetricsRequest)
                throws InternalServiceException, InvalidParameterValueException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.listMetricsPaginator(listMetricsRequest);
        }

        @Override
        public ListTagsForResourceResponse listTagsForResource(ListTagsForResourceRequest listTagsForResourceRequest)
                throws InvalidParameterValueException, ResourceNotFoundException, InternalServiceException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listTagsForResource(listTagsForResourceRequest);
        }

        @Override
        public ListTagsForResourceResponse listTagsForResource(
                Consumer<ListTagsForResourceRequest.Builder> listTagsForResourceRequest)
                throws InvalidParameterValueException, ResourceNotFoundException, InternalServiceException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.listTagsForResource(listTagsForResourceRequest);
        }

        @Override
        public PutAnomalyDetectorResponse putAnomalyDetector(PutAnomalyDetectorRequest putAnomalyDetectorRequest)
                throws LimitExceededException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, InvalidParameterCombinationException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putAnomalyDetector(putAnomalyDetectorRequest);
        }

        @Override
        public PutAnomalyDetectorResponse putAnomalyDetector(
                Consumer<PutAnomalyDetectorRequest.Builder> putAnomalyDetectorRequest) throws LimitExceededException,
                InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                InvalidParameterCombinationException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putAnomalyDetector(putAnomalyDetectorRequest);
        }

        @Override
        public PutCompositeAlarmResponse putCompositeAlarm(PutCompositeAlarmRequest putCompositeAlarmRequest)
                throws LimitExceededException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putCompositeAlarm(putCompositeAlarmRequest);
        }

        @Override
        public PutCompositeAlarmResponse putCompositeAlarm(Consumer<PutCompositeAlarmRequest.Builder> putCompositeAlarmRequest)
                throws LimitExceededException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putCompositeAlarm(putCompositeAlarmRequest);
        }

        @Override
        public PutDashboardResponse putDashboard(PutDashboardRequest putDashboardRequest)
                throws DashboardInvalidInputErrorException, InternalServiceException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.putDashboard(putDashboardRequest);
        }

        @Override
        public PutDashboardResponse putDashboard(Consumer<PutDashboardRequest.Builder> putDashboardRequest)
                throws DashboardInvalidInputErrorException, InternalServiceException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.putDashboard(putDashboardRequest);
        }

        @Override
        public PutInsightRuleResponse putInsightRule(PutInsightRuleRequest putInsightRuleRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, LimitExceededException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putInsightRule(putInsightRuleRequest);
        }

        @Override
        public PutInsightRuleResponse putInsightRule(Consumer<PutInsightRuleRequest.Builder> putInsightRuleRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, LimitExceededException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putInsightRule(putInsightRuleRequest);
        }

        @Override
        public PutManagedInsightRulesResponse putManagedInsightRules(
                PutManagedInsightRulesRequest putManagedInsightRulesRequest) throws InvalidParameterValueException,
                MissingRequiredParameterException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putManagedInsightRules(putManagedInsightRulesRequest);
        }

        @Override
        public PutManagedInsightRulesResponse putManagedInsightRules(
                Consumer<PutManagedInsightRulesRequest.Builder> putManagedInsightRulesRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putManagedInsightRules(putManagedInsightRulesRequest);
        }

        @Override
        public PutMetricAlarmResponse putMetricAlarm(PutMetricAlarmRequest putMetricAlarmRequest)
                throws LimitExceededException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putMetricAlarm(putMetricAlarmRequest);
        }

        @Override
        public PutMetricAlarmResponse putMetricAlarm(Consumer<PutMetricAlarmRequest.Builder> putMetricAlarmRequest)
                throws LimitExceededException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putMetricAlarm(putMetricAlarmRequest);
        }

        @Override
        public PutMetricDataResponse putMetricData(Consumer<PutMetricDataRequest.Builder> putMetricDataRequest)
                throws InvalidParameterValueException, MissingRequiredParameterException, InvalidParameterCombinationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putMetricData(putMetricDataRequest);
        }

        @Override
        public PutMetricStreamResponse putMetricStream(PutMetricStreamRequest putMetricStreamRequest)
                throws ConcurrentModificationException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, InvalidParameterCombinationException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putMetricStream(putMetricStreamRequest);
        }

        @Override
        public PutMetricStreamResponse putMetricStream(Consumer<PutMetricStreamRequest.Builder> putMetricStreamRequest)
                throws ConcurrentModificationException, InternalServiceException, InvalidParameterValueException,
                MissingRequiredParameterException, InvalidParameterCombinationException, AwsServiceException,
                SdkClientException, CloudWatchException {
            return CloudWatchClient.super.putMetricStream(putMetricStreamRequest);
        }

        @Override
        public SetAlarmStateResponse setAlarmState(SetAlarmStateRequest setAlarmStateRequest) throws ResourceNotFoundException,
                InvalidFormatException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.setAlarmState(setAlarmStateRequest);
        }

        @Override
        public SetAlarmStateResponse setAlarmState(Consumer<SetAlarmStateRequest.Builder> setAlarmStateRequest)
                throws ResourceNotFoundException, InvalidFormatException, AwsServiceException, SdkClientException,
                CloudWatchException {
            return CloudWatchClient.super.setAlarmState(setAlarmStateRequest);
        }

        @Override
        public StartMetricStreamsResponse startMetricStreams(StartMetricStreamsRequest startMetricStreamsRequest)
                throws InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.startMetricStreams(startMetricStreamsRequest);
        }

        @Override
        public StartMetricStreamsResponse startMetricStreams(
                Consumer<StartMetricStreamsRequest.Builder> startMetricStreamsRequest)
                throws InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.startMetricStreams(startMetricStreamsRequest);
        }

        @Override
        public StopMetricStreamsResponse stopMetricStreams(StopMetricStreamsRequest stopMetricStreamsRequest)
                throws InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.stopMetricStreams(stopMetricStreamsRequest);
        }

        @Override
        public StopMetricStreamsResponse stopMetricStreams(Consumer<StopMetricStreamsRequest.Builder> stopMetricStreamsRequest)
                throws InternalServiceException, InvalidParameterValueException, MissingRequiredParameterException,
                AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.stopMetricStreams(stopMetricStreamsRequest);
        }

        @Override
        public TagResourceResponse tagResource(TagResourceRequest tagResourceRequest)
                throws InvalidParameterValueException, ResourceNotFoundException, ConcurrentModificationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.tagResource(tagResourceRequest);
        }

        @Override
        public TagResourceResponse tagResource(Consumer<TagResourceRequest.Builder> tagResourceRequest)
                throws InvalidParameterValueException, ResourceNotFoundException, ConcurrentModificationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.tagResource(tagResourceRequest);
        }

        @Override
        public UntagResourceResponse untagResource(UntagResourceRequest untagResourceRequest)
                throws InvalidParameterValueException, ResourceNotFoundException, ConcurrentModificationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.untagResource(untagResourceRequest);
        }

        @Override
        public UntagResourceResponse untagResource(Consumer<UntagResourceRequest.Builder> untagResourceRequest)
                throws InvalidParameterValueException, ResourceNotFoundException, ConcurrentModificationException,
                InternalServiceException, AwsServiceException, SdkClientException, CloudWatchException {
            return CloudWatchClient.super.untagResource(untagResourceRequest);
        }

        @Override
        public CloudWatchWaiter waiter() {
            return CloudWatchClient.super.waiter();
        }
    }

}
