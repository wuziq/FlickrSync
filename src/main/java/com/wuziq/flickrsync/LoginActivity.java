package com.wuziq.flickrsync;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.example.R;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.wuziq.flickrsync.tasks.GetAccessTokenTask;
import com.wuziq.flickrsync.tasks.GetRequestTokenTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the entry point for authenticating against Flickr.
 * <p/>
 * It should be used only if the user has not authenticated.  Otherwise
 * it should never be seen.
 */
public class LoginActivity extends Activity
{
    private static final Logger logger = LoggerFactory.getLogger( LoginActivity.class );

    private Button m_button_authenticate;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        m_button_authenticate = (Button)findViewById( R.id.button_authenticate );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main,
                                   menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if ( id == R.id.action_settings )
        {
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    public void onAuthenticateClicked( View view )
    {
        // do login task
        GetRequestTokenTask getRequestTokenTask = new GetRequestTokenTask( this );
        getRequestTokenTask.execute();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // check whether we are returning here from authenticating via the browser
        Intent intent = getIntent();
        if ( null == intent )
        {
            // TODO:  log this?
            return;
        }

        String scheme = intent.getScheme();
        if ( ! scheme.equals( getString( R.string.scheme ) ) )
        {
            // not resumed from authenticating via browser
            return;
        }

        // check whether we've gotten request token already
        Preferences prefs = new Preferences();
        OAuth savedOAuthData = prefs.getOAuthTokenData();
        OAuthToken savedOAuthToken = savedOAuthData.getToken();
        User user = savedOAuthData.getUser();
        if ( null != savedOAuthToken && null == user )
        {
            // no request token was found
            // TODO:  why does lack of user mean there was no request token?  why not check for lack of the actual token???

            // TODO:  replace this ghetto-ass parser?
            Uri uri = intent.getData();
            String query = uri.getQuery();
            logger.debug( "Returned Query: {}",
                          query );
            String[] data = query.split( "&" );
            if ( data != null && data.length == 2 )
            {
                String newOAuthTokenString = data[0].substring( data[0].indexOf( "=" ) + 1 );
                String oAuthVerifier = data[1].substring( data[1].indexOf( "=" ) + 1 );
                logger.debug( "OAuth Token: {}; OAuth Verifier: {}",
                              newOAuthTokenString,
                              oAuthVerifier );

                // get access token
                // TODO:  what's the scenario where there's a request token but no user info???
                GetAccessTokenCallback callback = new GetAccessTokenCallback();
                GetAccessTokenTask getAccessTokenTask = new GetAccessTokenTask( callback );
                getAccessTokenTask.execute( newOAuthTokenString,
                                            savedOAuthToken.getOauthTokenSecret(),
                                            oAuthVerifier );
            }
        }
    }
}
