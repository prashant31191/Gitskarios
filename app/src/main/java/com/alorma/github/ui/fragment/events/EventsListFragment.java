package com.alorma.github.ui.fragment.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.ListEvents;
import com.alorma.github.sdk.bean.dto.response.events.EventType;
import com.alorma.github.sdk.bean.dto.response.events.payload.IssueCommentEventPayload;
import com.alorma.github.sdk.bean.info.IssueInfo;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.sdk.services.user.events.GetUserEventsClient;
import com.alorma.github.ui.activity.IssueDetailActivity;
import com.alorma.github.ui.activity.RepoDetailActivity;
import com.alorma.github.ui.adapter.events.EventAdapter;
import com.alorma.github.ui.fragment.base.PaginatedListFragment;
import com.google.gson.Gson;
import com.mikepenz.octicons_typeface_library.Octicons;

import retrofit.RetrofitError;

/**
 * Created by Bernat on 03/10/2014.
 */
public class EventsListFragment extends PaginatedListFragment<ListEvents>{

	private EventAdapter eventsAdapter;
	private String username;

	public static EventsListFragment newInstance(String username) {
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);

		EventsListFragment f = new EventsListFragment();
		f.setArguments(bundle);

		return f;
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().setTitle(R.string.menu_events);
	}

	@Override
	protected void onResponse(ListEvents githubEvents, boolean refreshing) {
		if (githubEvents != null && githubEvents.size() > 0) {

			if (eventsAdapter == null || refreshing) {
				eventsAdapter = new EventAdapter(getActivity(), githubEvents);
				setListAdapter(eventsAdapter);
			}

			if (eventsAdapter.isLazyLoading()) {
				if (eventsAdapter != null) {
					eventsAdapter.setLazyLoading(false);
					eventsAdapter.addAll(githubEvents);
				}
			}
			
			if (eventsAdapter != null) {
				setListAdapter(eventsAdapter);
			}
		} else if (eventsAdapter == null || eventsAdapter.getCount() == 0) {
			setEmpty();
		}
	}

	@Override
	public void onFail(RetrofitError error) {
		super.onFail(error);
		if (eventsAdapter == null || eventsAdapter.getCount() == 0) {
			setEmpty();
		}
	}

	@Override
	protected void loadArguments() {
		username = getArguments().getString(USERNAME);
	}

	@Override
	protected void executeRequest() {
		super.executeRequest();
		GetUserEventsClient eventsClient = new GetUserEventsClient(getActivity(), username);
		eventsClient.setOnResultCallback(this);
		eventsClient.execute();
	}

	@Override
	protected void executePaginatedRequest(int page) {
		super.executePaginatedRequest(page);

		eventsAdapter.setLazyLoading(true);

		GetUserEventsClient eventsClient = new GetUserEventsClient(getActivity(), username, page);
		eventsClient.setOnResultCallback(this);
		eventsClient.execute();
	}

	@Override
	protected Octicons.Icon getNoDataIcon() {
		return Octicons.Icon.oct_calendar;
	}

	@Override
	protected int getNoDataText() {
		return R.string.noevents;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.onItemClick(parent, view, position, id);
		GithubEvent item = eventsAdapter.getItem(position);
		EventType type = item.getType();
		if (type == EventType.IssueCommentEvent) {
			Gson gson = new Gson();
			String s = gson.toJson(item.payload);
			IssueCommentEventPayload payload = gson.fromJson(s, IssueCommentEventPayload.class);
			Issue issue = payload.issue;
			IssueInfo issueInfo = new IssueInfo();
			issueInfo.num = issue.number;
			String fullName = item.repo.name;
			String[] parts = fullName.split("/");
			issueInfo.repo =  new RepoInfo();
			issueInfo.repo.owner = parts[0];
			issueInfo.repo.name = parts[1];
			Intent launcherIntent = IssueDetailActivity.createLauncherIntent(getActivity(), issueInfo);
			startActivity(launcherIntent);
		} else {
			String fullName = item.repo.name;
			String[] parts = fullName.split("/");
			Intent intent = RepoDetailActivity.createLauncherIntent(getActivity(), parts[0], parts[1]);
			startActivity(intent);
		}
	}
}
