package com.jokes.test;

import android.content.Context;
import android.test.AndroidTestCase;

import com.udacity.gradle.builditbigger.JokesAsyncTask;

import org.mockito.Mock;

import java.util.concurrent.TimeUnit;


/**
 * Created by Akshata on 12/12/2016.
 * From solution posted by Matt_from_Pestulon at
 * https://discussions.udacity.com/t/async-task-test-where-to-even-begin/159593/3
 */

public class JokeTest extends AndroidTestCase {

    JokesAsyncTask task;
    String result;
    @Mock
    Context mockContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        result = null;
        task = new JokesAsyncTask() {
            @Override
            protected void onPostExecute(String joke) {

            }
        };
    }

    public void testAsyncReturnType() {

        try {

            task.execute(mockContext);
            result = task.get(10, TimeUnit.SECONDS);
            assertNotNull(result);

        } catch (Exception e) {
            fail("Timed out");
        }
    }
}