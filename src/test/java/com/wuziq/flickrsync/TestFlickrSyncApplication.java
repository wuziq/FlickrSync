package com.wuziq.flickrsync;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import mockit.Mocked;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.TestLifecycleApplication;
import roboguice.RoboGuice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuziq on 5/21/2014.
 */
public class TestFlickrSyncApplication extends FlickrSyncApplication implements
                                                                     TestLifecycleApplication
{
    @Override public void beforeTest( Method test )
    {}

    @Override public void prepareTest( Object test )
    {
        MockitoAnnotations.initMocks( test );
        try
        {
            setUpRoboguice( test, getListOfMocks( test ) );
        } catch ( IllegalAccessException e )
        {
            throw new RuntimeException( "Failed to get instance of object", e );
        }
    }

    @Override public void afterTest( Method method )
    {}

    private List<Object> getListOfMocks( Object test ) throws IllegalAccessException
    {
        final Field[] declaredFields = test.getClass().getDeclaredFields();
        List<Object> objects = new ArrayList<Object>();
        for ( Field field : declaredFields )
        {
            // TODO:  get jmockit mocks to work
            if (    field.getAnnotation( Mock.class ) != null
                 || field.getAnnotation( Mocked.class ) != null )
            {
                field.setAccessible( true );
                objects.add( field.get( test ) );
            }
        }
        return objects;
    }

    private void setUpRoboguice( Object test, List<Object> objects )
    {
        TestFlickrSyncApplication application = (TestFlickrSyncApplication)Robolectric.application;

        RoboGuice.setBaseApplicationInjector( application,
                                              RoboGuice.DEFAULT_STAGE,
                                              RoboGuice.newDefaultRoboModule( application ),
                                              new MyRoboModule( objects ) );

        RoboGuice.getInjector( application )
                 .injectMembers( test );
    }

    public static class MyRoboModule extends AbstractModule
    {
        private List<Object> mocksToInject;

        public MyRoboModule( List<Object> mocksToInject )
        {
            this.mocksToInject = mocksToInject;
        }

        @Override
        protected void configure()
        {
            for ( final Object mock : mocksToInject )
            {
                Class mockedClass = mock.getClass();
                Class[] interfaces = mockedClass.getInterfaces();
                for ( Class myInterface : interfaces )
                {
                    if ( myInterface.getName().contains( "wuziq" ) )
                    {
                        bind( myInterface ).toInstance( mock );
                    }
                }
            }
        }
    }
}
