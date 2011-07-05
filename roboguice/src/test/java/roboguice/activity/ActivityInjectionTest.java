package roboguice.activity;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import roboguice.RoboGuice;
import roboguice.activity.ActivityInjectionTest.ModuleA.A;
import roboguice.activity.ActivityInjectionTest.ModuleB.B;
import roboguice.activity.ActivityInjectionTest.ModuleC.C;
import roboguice.activity.ActivityInjectionTest.ModuleD.D;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectPreference;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.TextView;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Stage;

import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ActivityInjectionTest {

    protected DummyActivity activity;
    protected DummyPreferenceActivity prefsActivity;
    
    @Before
    public void setup() {
        RoboGuice.setApplicationInjector(Robolectric.application, Stage.DEVELOPMENT, RoboGuice.createNewDefaultRoboModule(Robolectric.application), new ModuleA());
        activity = new DummyActivity();
        activity.setIntent( new Intent(Robolectric.application,DummyActivity.class).putExtra("foobar","goober") );
        activity.onCreate(null);

        prefsActivity = new DummyPreferenceActivity();
        prefsActivity.onCreate(null);
    }

    @Test
    public void shouldInjectUsingDefaultConstructor() {
        assertThat(activity.emptyString,is(""));
    }

    @Test
    public void shouldInjectView() {
        assertThat(activity.text1,is(activity.findViewById(R.id.text1)));
    }

    @Test
    public void shouldInjectStringResource() {
        assertThat(activity.cancel,is("Cancel"));
    }

    @Test
    public void shouldInjectExtras() {
        assertThat(activity.foobar,is("goober"));
    }

    // BUG This doesn't work yet because createNewPreferenceScreen doesn't properly model whatever's goign on
    @Test @Ignore
    public void shouldInjectPreference() {
        assertThat(prefsActivity.pref, is(prefsActivity.findPreference("xxx")));
    }

    @Test
    public void shouldStaticallyInject() {
        assertThat(A.t, equalTo(""));
    }

    @Test
    public void shouldStaticallyInjectResources() {
        assertThat(A.s,equalTo("Cancel"));
    }

    @Test(expected = ConfigurationException.class)
    public void shouldNotStaticallyInjectViews() {
        RoboGuice.setApplicationInjector(Robolectric.application, Stage.DEVELOPMENT, RoboGuice.createNewDefaultRoboModule(Robolectric.application), new ModuleB());
        final B b = new B();
        b.onCreate(null);
    }

    @Test(expected = ConfigurationException.class)
    public void shouldNotStaticallyInjectExtras() {
        RoboGuice.setApplicationInjector(Robolectric.application, Stage.DEVELOPMENT, RoboGuice.createNewDefaultRoboModule(Robolectric.application), new ModuleD());
        final D d = new D();
        d.onCreate(null);
    }

    @Test(expected = ConfigurationException.class)
    public void shouldNotStaticallyInjectPreferenceViews() {
        RoboGuice.setApplicationInjector(Robolectric.application, Stage.DEVELOPMENT, RoboGuice.createNewDefaultRoboModule(Robolectric.application), new ModuleC());
        final C c = new C();
        c.onCreate(null);
    }


    /*
    'This is supposed to reflect the failure Im seeing in MyGroupons where context goes out of scope'
    "E/COM.GROUPON/Tracking.java:94(30516): Caused by: java.lang.UnsupportedOperationException: Can't perform injection outside of a context scope\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat roboguice.inject.ContextScope$1.get(ContextScope.java:87)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.InternalFactoryToProviderAdapter.get(InternalFactoryToProviderAdapter.java:48)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.InjectorImpl$4$1.call(InjectorImpl.java:758)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.InjectorImpl.callInContext(InjectorImpl.java:811)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.InjectorImpl$4.get(InjectorImpl.java:754)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat roboguice.inject.SharedPreferencesProvider.get(SharedPreferencesProvider.java:50)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat roboguice.inject.SharedPreferencesProvider.get(SharedPreferencesProvider.java:29)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.BoundProviderFactory.get(BoundProviderFactory.java:58)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.InjectorImpl$4$1.call(InjectorImpl.java:758)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.InjectorImpl.callInContext(InjectorImpl.java:804)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.google.inject.InjectorImpl$4.get(InjectorImpl.java:754)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.groupon.service.CountryService.getCurrentCountryData(CountryService.java:134)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.groupon.service.CountryService.isUSACompatible(CountryService.java:186)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.groupon.service.CountryService.getCurrentBaseUrl(CountryService.java:66)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.groupon.http.synchronous.SyncHttp.getURI(SyncHttp.java:167)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.groupon.http.synchronous.SyncHttp.call(SyncHttp.java:96)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.groupon.http.synchronous.SyncHttp.callOnlyUsingRemote(SyncHttp.java:91)\n"+
            "E/COM.GROUPON/Tracking.java:94(30516): \tat com.groupon.http.synchronous.HttpO\n"+
            "D/COM.GROUPON/Tracking.java:110(30516): 01:47:47.090 pool-1-thread-17 Sending exception (Guice provision errors:\n"+
            "D/COM.GROUPON/Tracking.java:110(30516): \n"+
            "D/COM.GROUPON/Tracking.java:110(30516): 1) Error in custom provider, java.lang.UnsupportedOperationException: Can't perform injection outside of a context scope\n"+
            "D/COM.GROUPON/Tracking.java:110(30516):   while locating android.content.Context\n"+
            "D/COM.GROUPON/Tracking.java:110(30516):   while locating roboguice.inject.SharedPreferencesProvider\n"+
            "D/COM.GROUPON/Tracking.java:110(30516):   while locating android.content.SharedPreferences\n"+
            "D/COM.GROUPON/Tracking.java:110(30516): \n"+
            "D/COM.GROUPON/Tracking.java:110(30516): 1 error) to flurry"
            */
    @Test
    public void shouldAllowBackgroundThreadsToFinishUsingContextAfterOnDestroy() throws Exception {
        final SoftReference<F> ref = new SoftReference<F>(new F());
        ref.get().onCreate(null);

        final FutureTask<Context> future = new FutureTask<Context>( new Callable<Context>() {
            Provider<Context> contextProvider = RoboGuice.getInjector(ref.get()).getProvider(Context.class);

            @Override
            public Context call() throws Exception {
                return contextProvider.get();
            }
        });

        ref.get().onDestroy();

        // Force an OoM
        // http://stackoverflow.com/questions/3785713/how-to-make-the-java-system-release-soft-references/3810234
        try {
            @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"}) final ArrayList<Object[]> allocations = new ArrayList<Object[]>();
            //noinspection InfiniteLoopStatement
            while(true)
                allocations.add( new Object[(int) Runtime.getRuntime().maxMemory()] );
        } catch( OutOfMemoryError e ) {
            // great!
        }

        Executors.newSingleThreadExecutor().execute(future);


        assertNotNull(future.get());

    }

    public static class E extends RoboActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }



    public static class DummyActivity extends RoboActivity {
        @Inject protected String emptyString;
        @InjectView(R.id.text1) protected TextView text1;
        @InjectResource(R.string.cancel) protected String cancel;
        @InjectExtra("foobar") protected String foobar;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final TextView root = new TextView(this);
            root.setId(R.id.text1);                       
            setContentView(root);
        }
    }

    public static class DummyPreferenceActivity extends RoboPreferenceActivity {
        @Nullable @InjectPreference("xxx") protected Preference pref;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final PreferenceScreen screen = createNewPreferenceScreen();

            final Preference p = new CheckBoxPreference(this);
            p.setKey("xxx");

            screen.addPreference(p);

            setPreferenceScreen(screen);
        }

        protected PreferenceScreen createNewPreferenceScreen() {

            try {
                final Constructor<PreferenceScreen> c = PreferenceScreen.class.getDeclaredConstructor();
                c.setAccessible(true);
                @SuppressWarnings({"UnnecessaryLocalVariable"}) final PreferenceScreen screen = c.newInstance();

                /*
                final Method m = PreferenceScreen.class.getMethod("onAttachedToHierarchy");
                m.setAccessible(true);
                m.invoke(this);
                */

                return screen;
                
            } catch( Exception e ) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ModuleA extends com.google.inject.AbstractModule {
        @Override
        protected void configure() {
            requestStaticInjection(A.class);
        }


        public static class A {
            @InjectResource(android.R.string.cancel) static String s;
            @Inject static String t;
        }
    }


    public static class ModuleB extends com.google.inject.AbstractModule {
        @Override
        public void configure() {
            requestStaticInjection(B.class);
        }


        public static class B extends RoboActivity{
            @InjectView(0) static View v;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
            }
        }
    }


    public static class ModuleC extends com.google.inject.AbstractModule {
        @Override
        public void configure() {
            requestStaticInjection(C.class);
        }


        public static class C extends RoboActivity{
            @InjectPreference("xxx") static Preference v;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
            }
        }
    }


    public static class ModuleD extends com.google.inject.AbstractModule {
        @Override
        public void configure() {
            requestStaticInjection(D.class);
        }


        public static class D extends RoboActivity{
            @InjectExtra("xxx") static String s;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
            }
        }
    }

    public static class F extends RoboActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
        }
    }
}