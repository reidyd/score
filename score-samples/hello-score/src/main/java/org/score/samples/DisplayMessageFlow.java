package org.score.samples;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import org.apache.log4j.Logger;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.InputBinding;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 8/18/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class DisplayMessageFlow {
	private final static Logger logger = Logger.getLogger(DisplayMessageFlow.class);
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String USER = "user";
	private List<InputBinding> inputBindings;

	public DisplayMessageFlow() {
		inputBindings = generateInitialInputBindings();
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>();

		bindings.add(InputBinding.createInputBinding("status", "status", true));
		bindings.add(InputBinding.createInputBinding("message", "message", true));
		bindings.add(InputBinding.createInputBinding("user", "user", true));

		return bindings;
	}

	public TriggeringProperties displayMessageFlow() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(createDisplayExecutionPlan());
		Map<String, Serializable> context = new HashMap<>();
		triggeringProperties.setContext(context);
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public ExecutionPlan createDisplayExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 1L));
		builder.addOOActionStep(0L, "org.score.samples.DisplayMessageFlow", "displayMessage", null, navigationMatchers);

		builder.addOOActionFinalStep(1L, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");

		return builder.createTriggeringProperties().getExecutionPlan();
	}

	public Map<String, String> displayMessage(String message, String status, String user) {
		logger.info(status + " -> " + user + " : " + message);
		return new HashMap<>();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}
}