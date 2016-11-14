package com.hyphenate.chatuidemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.chat.ConversationListFragment;
import com.hyphenate.chatuidemo.ui.group.InviteMembersActivity;
import com.hyphenate.chatuidemo.ui.group.PublicGroupsListActivity;
import com.hyphenate.chatuidemo.ui.settings.SettingsFragment;
import com.hyphenate.chatuidemo.ui.sign.SignInActivity;
import com.hyphenate.chatuidemo.ui.user.AddContactsActivity;
import com.hyphenate.chatuidemo.ui.user.ContactListFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wei on 2016/9/27.
 * The main activity of demo app
 */
public class MainActivity extends BaseActivity {
    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.view_pager) ViewPager mViewPager;

    private int mCurrentPageIndex = 0;

    private ConversationListFragment mConversationListFragment;
    private ContactListFragment mContactListFragment;
    private SettingsFragment mSettingsFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        // Check that you are logged in
        if (EMClient.getInstance().isLoggedInBefore()) {
            // Load the group into memory
            EMClient.getInstance().groupManager().loadAllGroups();
            // Load all mConversation into memory
            EMClient.getInstance().chatManager().loadAllConversations();
        } else {
            // Go sign in
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }

        // Set default setting values
        //PreferenceManager.setDefaultValues(this, R.xml.preferences_default, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_main);
        ButterKnife.bind(this);
        //setup viewpager
        setupViewPager();
        //setup tabLayout with viewpager
        setupTabLayout();
    }

    private void setupViewPager() {
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        mContactListFragment = ContactListFragment.newInstance();
        mConversationListFragment = ConversationListFragment.newInstance();
        mSettingsFragment = SettingsFragment.newInstance();
        //add fragments to adapter
        adapter.addFragment(mContactListFragment, "Contacts");
        adapter.addFragment(mConversationListFragment, "Chats");
        adapter.addFragment(mSettingsFragment, "Settings");
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageSelected(int position) {
                mCurrentPageIndex = position;
                Toolbar toolbar = getActionBarToolbar();
                toolbar.setTitle(adapter.getPageTitle(position));
                toolbar.getMenu().clear();
                if (position == 0) { //Contacts
                    toolbar.inflateMenu(R.menu.em_contacts_menu);
                    mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.img_tab_item);
                } else if (position == 1) { //Chats
                    toolbar.inflateMenu(R.menu.em_conversations_menu);
                }
                if (position != 2) {
                    setSearchViewQueryListener();
                }
            }

            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setupTabLayout() {
        mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.tab_indicator));
        mTabLayout.setupWithViewPager(mViewPager);
        //        mTabLayout.setSelectedTabIndicatorHeight(R.dimen.tab_indicator_height);
        for (int i = 0; i < 3; i++) {
            View customTab = LayoutInflater.from(this).inflate(R.layout.em_tab_layout_item, null);
            ImageView imageView = (ImageView) customTab.findViewById(R.id.img_tab_item);
            if (i == 0) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.em_tab_contacts_selector));
            } else if (i == 1) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.em_tab_chats_selector));
            } else {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.em_tab_settings_selector));
            }
            //set the custom tabview
            mTabLayout.getTabAt(i).setCustomView(customTab);
        }

    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //add the action buttons to toolbar
        Toolbar toolbar = getActionBarToolbar();
        if (mViewPager.getCurrentItem() == 0) {
            toolbar.inflateMenu(R.menu.em_contacts_menu);
            setSearchViewQueryListener();
        } else if (mViewPager.getCurrentItem() == 1) {
            toolbar.inflateMenu(R.menu.em_conversations_menu);
            setSearchViewQueryListener();
        }
        toolbar.setOnMenuItemClickListener(new ToolBarMenuItemClickListener());
        return true;
    }

    private void setSearchViewQueryListener(){
        Toolbar toolbar = getActionBarToolbar();
        SearchView searchView;
        if(mCurrentPageIndex == 0){
            searchView = (SearchView)toolbar.getMenu().findItem(R.id.menu_search).getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) {
                    return true;
                }

                @Override public boolean onQueryTextChange(String newText) {
                    //ContactListFragment.newInstance().filter(newText);
                    return true;
                }
            });
        }else if(mCurrentPageIndex == 1){
            searchView = (SearchView) MenuItemCompat.getActionView(
                    toolbar.getMenu().findItem(R.id.menu_conversations_search));
            // search conversations list
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) {
                    mConversationListFragment.filter(query);
                    return true;
                }

                @Override public boolean onQueryTextChange(String newText) {
                    mConversationListFragment.filter(newText);
                    return true;
                }
            });
        }
    }

    /**
     * Toolbar menu item onclick listener
     */
    private class ToolBarMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        @Override public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

                case R.id.menu_create_group:

                    startActivity(new Intent(MainActivity.this, InviteMembersActivity.class));
                    break;

                case R.id.menu_public_groups:
                    startActivity(new Intent(MainActivity.this, PublicGroupsListActivity.class));
                    break;
                case R.id.menu_add_contacts:
                    startActivity(new Intent(MainActivity.this, AddContactsActivity.class));
                    break;
            }

            return false;
        }
    }

    /**
     * message listener
     */
    EMMessageListener mMessageListener = new EMMessageListener() {
        @Override public void onMessageReceived(List<EMMessage> list) {
            Fragment fragment = null;
            //display unread tips
            if (EMClient.getInstance().chatManager().getUnreadMessageCount() > 0) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        getTabUnreadStatusView(1).setVisibility(View.VISIBLE);
                    }
                });
            }
            //refresh ConversationListFragment
            fragment = ((PagerAdapter) mViewPager.getAdapter()).getItem(1);
            ((ConversationListFragment) fragment).refresh();
        }

        @Override public void onCmdMessageReceived(List<EMMessage> list) {
        }

        @Override public void onMessageRead(List<EMMessage> list) {
        }

        @Override public void onMessageDelivered(List<EMMessage> list) {
        }

        @Override public void onMessageChanged(EMMessage emMessage, Object o) {
        }
    };

    private ImageView getTabUnreadStatusView(int index) {
        View tabView = mTabLayout.getTabAt(index).getCustomView();
        ImageView unreadStatusView = (ImageView) tabView.findViewById(R.id.img_unread_status);
        return unreadStatusView;
    }

    @Override protected void onResume() {
        super.onResume();
        //register message listener
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);

        updateUnreadMsgLabel();
    }

    @Override protected void onStop() {
        super.onStop();
        //unregister message listener on stop
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void updateUnreadMsgLabel() {
        if (EMClient.getInstance().chatManager().getUnreadMessageCount() > 0) {
            getTabUnreadStatusView(1).setVisibility(View.VISIBLE);
        } else {
            getTabUnreadStatusView(1).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Fragment pager adapter
     */
    private class PagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override public int getCount() {
            return mFragments.size();
        }

        @Override public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
