package com.hp.oo.openstack.actions;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.Score;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.anything;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Date: 7/22/2014
 *
 * @author Bonczidai Levente
 */
public class ExecutionPlanBuilder {
	public static final String ACTION_CLASS_KEY = "className";
	public static final String ACTION_METHOD_KEY = "methodName";
	private static Long stepCount;

	private ExecutionPlan executionPlan;

	public ExecutionPlanBuilder() {
		executionPlan = new ExecutionPlan();
		executionPlan.setFlowUuid(UUID.randomUUID().toString());
		stepCount = 0L;
		executionPlan.setBeginStep(stepCount);
	}

	public ExecutionPlan getExecutionPlan() {
		return executionPlan;
	}

	public Long addStep(String actionClassName,
						String actionMethodName, List<NavigationMatcher> navigationMatchers, String defaultNextStepId) {
		ExecutionStep step = new ExecutionStep(stepCount++);

		step.setAction(new ControlActionMetadata("com.hp.oo.openstack.actions.OOActionRunner", "run"));
		Map<String, String> actionData = new HashMap<>(2);
		//put the actual action class name and method name
		actionData.put(ACTION_CLASS_KEY, actionClassName);
		actionData.put(ACTION_METHOD_KEY, actionMethodName);
		step.setActionData(actionData);

		step.setNavigation(new ControlActionMetadata("com.hp.oo.openstack.actions.OOActionRunner", "navigate"));
		Map<String, Object> navigationData = new HashMap<>(1);
		navigationData.put("navigationMatchers", navigationMatchers);
		navigationData.put("defaultNextStepId", defaultNextStepId);

		step.setNavigationData(navigationData);

		step.setSplitStep(false);


		executionPlan.addStep(step);

		return step.getExecStepId();
	}

	@SuppressWarnings("unused")
	public void setBeginStep(Long beginStepId) {
		executionPlan.setBeginStep(beginStepId);
	}
	// add final step
	public static void main(String[] args)
	{
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		List<NavigationMatcher> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher(MatchType.COMPARE_EQUAL, "result", "200", "1"));
		//navigationMatchers.add(new NavigationMatcher(MatchType.COMPARE_NOT_EQUAL, "result", "200", "2"));y


		builder.addStep("com.hp.oo.openstack.actions.HttpClientPostMock", "post", navigationMatchers, "2");

		navigationMatchers = new ArrayList<>(); // doesnt work if using the same reference
		navigationMatchers.add(new NavigationMatcher(MatchType.COMPARE_EQUAL, "result", "400", "2"));

		builder.addStep("com.hp.oo.openstack.actions.HttpClientSendEmailMock", "sendEmail", navigationMatchers, "2");

		navigationMatchers = null;

		builder.addStep("com.hp.oo.openstack.actions.ReturnStepActions", "successStepAction", navigationMatchers, "2");



		ApplicationContext context = loadScore();
		ExecutionPlan executionPlan = builder.getExecutionPlan();
		Score score = context.getBean(Score.class);
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.put("username", "user");

		score.trigger(executionPlan, executionContext);
	}

	private static ApplicationContext loadScore() {
		return new ClassPathXmlApplicationContext("/META-INF/spring/helloScoreContext.xml");
	}
}
