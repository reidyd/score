package org.score.samples;


import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.events.EventBus;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import org.apache.log4j.Logger;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;
import org.score.samples.openstack.actions.OOActionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 7/28/2014
 *
 * @author Bonczidai Levente
 */
public class OpenstackApplications {
	private final static Logger logger = Logger.getLogger(OpenstackApplications.class);
	private ApplicationContext context;

	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	public static void main(String[] args) {
		OpenstackApplications app = loadApp();
		app.registerEventListeners();
		app.start();
	}

	@SuppressWarnings("unchecked")
	private void start() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		List<NavigationMatcher> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher(MatchType.EQUAL, "returnCode", "0", 1L)); // how will we know the key
		navigationMatchers.add(new NavigationMatcher(MatchType.DEFAULT, 2L));

		//builder.addStep(0L, "org.score.samples.openstack.actions.HttpClientPostMock", "post", navigationMatchers);
		builder.addStep(0L, "org.score.content.httpclient.HttpClientAction", "execute", navigationMatchers);

				navigationMatchers = new ArrayList<>(); // doesnt work if using the same reference
		navigationMatchers.add(new NavigationMatcher(MatchType.EQUAL, "result", "400", 2L));
		navigationMatchers.add(new NavigationMatcher(MatchType.DEFAULT, 2L));

		builder.addStep(1L, "org.score.samples.openstack.actions.HttpClientSendEmailMock", "sendEmail", navigationMatchers);

		builder.addFinalStep(2L, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");

		ExecutionPlan executionPlan = builder.getExecutionPlan();

		Map<String, Serializable> executionContext = new HashMap<>();
		prepareExecutionContext(executionContext);

		score.trigger(executionPlan, executionContext);
	}

	private void prepareExecutionContext(Map<String, Serializable> executionContext) {
		//for post
//		executionContext.put("username", "userTest");
//		executionContext.put("password", "passTest");
//		executionContext.put("host", "hostTest");
//		executionContext.put("url", "urlTest");

		executionContext.put("url", "http://16.77.61.83:5000/v2.0/tokens");
		executionContext.put("method", "post");
		executionContext.put("body", "{\"auth\":{\"passwordCredentials\":{\"username\":\"admin\",\"password\":\"B33f34t3r\"},\"tenantId\":\"612d32e9cac84e27be88828ab9b03383\"}}");
		executionContext.put("contentType", "application/json");

		//for sendEmail
		executionContext.put("receiver", "receiverTest");
		executionContext.put("title", "titleTest");
		//executionContext.put("body", "bodyTest");
	}

	private static OpenstackApplications loadApp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/openstackApplicationContext.xml");
		OpenstackApplications app = context.getBean(OpenstackApplications.class);
		app.context  = context;
		return app;
	}

	private void closeContext() {
		((ConfigurableApplicationContext) context).close();
	}

	private void registerEventListeners() {
		//register listener for action runtime events
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE);
		registerInfoEventListener(handlerTypes);

		//register listener for action exception events
		registerExceptionEventListener();

		// for closing the Application Context when score finishes execution
		registerScoreEventListener();
	}

	private void registerExceptionEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE);
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerInfoEventListener(Set<String> handlerTypes) {
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerScoreEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add("FINISHED");
		handlerTypes.add("ERROR");
		handlerTypes.add("CANCELLED");
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
				closeContext();
			}
		}, handlerTypes);
	}

	private void logListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
	}
}