package com.hp.score.samples.openstack;

import com.hp.score.api.TriggeringProperties;
import com.hp.score.samples.openstack.actions.ExecutionPlanBuilder;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.samples.openstack.actions.InputBindingFactory;
import com.hp.score.samples.openstack.actions.MatchType;
import com.hp.score.samples.openstack.actions.NavigationMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.samples.openstack.OpenstackCommons.*;


/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
public class ListServersFlow {
	private List<InputBinding> inputBindings;

	public ListServersFlow() {
		inputBindings = generateInitialInputBindings();
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>(5);

		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_HOST_MESSAGE, HOST_KEY, true));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(IDENTITY_PORT_MESSAGE, IDENTITY_PORT_KEY, true, DEFAULT_IDENTITY_PORT));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(COMPUTE_PORT_MESSAGE, COMPUTE_PORT_KEY, true, DEFAULT_COMPUTE_PORT));
		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_USERNAME_MESSAGE, USERNAME_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_PASSWORD_MESSAGE, PASSWORD_KEY, true));

		return bindings;
	}

	public TriggeringProperties listServersFlow() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder("list servers");

		Long prepareGetTokenId = 0L;
		Long authenticationStepId = 1L;
		Long parseAuthenticationStepId = 2L;
		Long mergerStepId = 3L;
		Long getServersStepId = 4L;
		Long displayStepId = 5L;
		Long successStepId = 6L;
		Long failureStepId = 7L;
		Long prepareParseAuthenticationStepId = 8L;
		Long prepareDisplayStepId = 9L;

		createPrepareGetAuthenticationStep(builder, prepareGetTokenId, authenticationStepId);

		createGetAuthenticationStep(builder, authenticationStepId, prepareParseAuthenticationStepId, failureStepId);

		createPrepareParseAuthenticationStep(builder, prepareParseAuthenticationStepId, parseAuthenticationStepId);

		createParseAuthenticationStep(builder, parseAuthenticationStepId, mergerStepId, failureStepId);

		createPrepareGetServersStep(builder, mergerStepId, getServersStepId);

		createGetServersStep(builder, getServersStepId, prepareDisplayStepId, failureStepId);

		createPrepareDisplayStep(builder, prepareDisplayStepId, displayStepId);

		createDisplayStep(builder, displayStepId, successStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		Map<String, Serializable> context = new HashMap<>();
		builder.setInitialExecutionContext(context);

		builder.setBeginStep(0L);

		return builder.createTriggeringProperties();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}

	private void createPrepareDisplayStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId){
		List<InputBinding> inputs  = new ArrayList<>(1);

		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(GET_SERVERS_RESPONSE_KEY, RETURN_RESULT_KEY));

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs, nextStepId);
	}

	private void createDisplayStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>(1);
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, nextStepId));
		builder.addOOActionStep(stepId, OPENSTACK_UTILS_CLASS, GET_SERVER_NAMES_METHOD, null, navigationMatchers);
	}

	private void createParseAuthenticationStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(1);

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RETURN_CODE, SUCCESS_CODE, successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, OPENSTACK_UTILS_CLASS, PARSE_AUTHENTICATION_METHOD, null, navigationMatchers);
	}

}