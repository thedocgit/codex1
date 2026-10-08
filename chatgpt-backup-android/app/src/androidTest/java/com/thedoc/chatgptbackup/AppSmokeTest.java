package com.thedoc.chatgptbackup;
import android.content.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test; import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class AppSmokeTest {
 @Test public void launchesAndPersistsConversation() throws Exception {
  try(ActivityScenario<MainActivity> s=ActivityScenario.launch(MainActivity.class)){
   s.onActivity(a->{assertNotNull(a.web); assertNotNull(a.status);
    a.getSharedPreferences("backup",Context.MODE_PRIVATE).edit().putString("c_test","{\"id\":\"test\",\"title\":\"Teste\",\"messages\":[{\"role\":\"user\",\"text\":\"oi\"}]}").commit();
    assertEquals(1,a.count());
   });
  }
 }
}