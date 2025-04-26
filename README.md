```markdown
DrawGraphic.java
```
### Написаны классы, помогающие строить графики в Java для Android.
На одном графике можно строить несколько линий.<br>
Линии создаются путём создания `HashMap <Integer, Double>`
для меток времени и соответствующих значений. <br>
Эти значения вносятся в создание класса Line. <br>
Класс Line создаётся конструктором по примеру:<br>
`Line l1 = new Line(timeData, data1, Color.RED, "График 1");`<br>
с указанием значений, цвета линии и её подписи. <br>
Линии записываются в массив линий по примеру:
```java
HashMap<Integer, Line> dat = new HashMap<>();
Line l1 = new Line(timeData, data1, Color.RED, "График 1");
Line l2 = new Line(timeData, data2, Color.BLUE, "График 2");
Line l3 = new Line(timeData, data3, Color.GREEN, "График 3");
dat.put(1, l1);
dat.put(2, l2);
dat.put(3, l3);
```
Далее создаётся элемент класса DrawGrphic, по примеру:<br>
`drawView = new DrawGraphic(this, 1000, 800, 11, 11, 1, "Скорость", "Секунды", dat, 0);`<br>
, где параметры - это: контекст, ширина и высота экрана, количество делений по вертикали и горизонтали, длина окна графика в единицах оси х, подписи вертикальной и горизонтальных осей графика, массив линий и флаг - показан ли полноэкранный режим. <br>
Можно использовать другой конструктор: <br>`drawView = new DrawGraphic(this, 1000, 800, 11, 11, 3, "Скорость", "Секунды", dat, 0, 11,3);`<br>
, куда добавляются минимальная длина и высота окна в единицах осей х и y. <br>
Для блокировки графиков можно мспользовать запрет реакций: `drawView.react = 0;`. Для установления больших отступов можно прописать строку: `drawView.setPaddMarg(60,40);`, отступы должны быть не менее 50 и 30.<br>
drawView добавляется во ViewGroup для отображения графиков.<br>
В каждом элементе класса сохраняется сериализируемый параметр класса info, который можно передать
в другие activity и создать график на основе него с помощью конструктора `drawView = new DrawGraphic(this, i, 1);`, где 1 - показатель полноэкранного режима.
### Возможности графиков
График в режиме активированных реакций (по умолчанию) можно передвигать, увеличивать, уменьшать, растягивать и стягивать по горизонтали и вертикали.<br>
На нём можно просматривать точки. При нажатии на точку можно выбрать другую точку линии, проведя пальцем вниз, 
а также можно зафиксировать точку, проведя пальцем вверх. Таким же образом точки можно удалять.<br>
Есть возможность показа легенд (подписей линий).
### Авторские права
Программе присвоен номер государственной регистрации 2025660591.<br>
https://www.fips.ru/publication-web/publications/document?type=doc&tab=PrEVM&id=DECBB4E2-25C6-4EF8-9F34-5520ECD55314 <br>
При использовании программы ссылайтесь на автора!<br>
<br>
### Classes have been written to help build graphs in Java for Android.
You can build multiple lines on a single chart.<br>
The lines are created by creating a `HashMap <Integer, Double>`
for the timestamps and corresponding values. <br>
These values are added to the creation of the Line class. <br>
The Line class is created by the constructor using the example:<br>
`Line l1 = new Line(timeData, data1, Color.RED, "Graph 1");`<br>
indicating the values, color of the line and its signature. <br>
Lines are written to an array of lines using the example:
```java
HashMap<Integer, Line> dat = new HashMap<>();
Line l1 = new Line(timeData, data1, Color.RED, "Graph 1");
Line l2 = new Line(timeData, data2, Color.BLUE, "Graph 2");
Line l3 = new Line(timeData, data3, Color.GREEN, "Graph 3");
dat.put(1, l1);
dat.put(2, l2);
dat.put(3, l3);
```
Next, an element of the DrawGrphic class is created, following the example:<br>
`DrawView = new DrawGraphic(this, 1000, 800, 11, 11, 1, " Speed", "Seconds", dat, 0);`<br>
, where the parameters are: the context, the width and height of the screen, the number of vertical and horizontal divisions, the length of the graph window in x-axis units, the labels of the vertical and horizontal axes of the graph, an array of lines and a flag indicating whether fullscreen mode is shown. <br>
You can use a different constructor: <br>`DrawView = new DrawGraphic(this, 1000, 800, 11, 11, 3, " Speed", "Seconds", dat, 0, 11.3);`<br>
, where the minimum length and height of the window are added in units of the x and y axes. <br>
To block graphs, you can use the reaction ban: `DrawView.react = 0;`. To set large margins, you can specify the line: `DrawView.setPaddMarg(60,40);`, the margins must be at least 50 and 30.<br>
DrawView is added to the ViewGroup to display graphs.<br>
Each class element stores a serializable parameter of the info class, which can be passed
to other activities and a graph can be created based on it using the constructor `DrawView = new DrawGraphic(this, i, 1);`, where 1 is the indicator of fullscreen mode.
### Graph features
The graph in the activated reactions mode (by default) can be moved, enlarged, decreased, stretched and tightened horizontally and vertically.<br>
You can view points on it. When clicking on a point, you can select another point on the line by swiping down,
or you can lock the point by swiping up. You can delete points in the same way.<br>
It is possible to display legends (line signatures).
### Copyright
The program has been assigned the state registration number 2025660591.<br>
https://www.fips.ru/publication-web/publications/document?type=doc&tab=PrEVM&id=DECBB4E2-25C6-4EF8-9F34-5520ECD55314 <br>
When using the program, refer to the author!<br>
<br>
### Пример использования программы
### Example of using the program
##### activity_main.xml и activity_two.xml:
```xml
  <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:orientation="vertical" >
<FrameLayout
android:id="@+id/graph_view"
android:layout_width="match_parent"
android:layout_height="0dp"
android:layout_weight="1" />
</LinearLayout>
```
##### AndroidManifest.xml:
```xml
  <?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.myapplication.draw"
    android:versionCode="1"
	android:versionName="1.0">
    <uses-sdk android:minSdkVersion="16" android:targetSdkVersion="31"/>
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
		android:name=".MainActivity"
		android:noHistory="true"
		android:label="@string/app_name"
		android:exported="true">
		<intent-filter>
		<action android:name="android.intent.action.MAIN" />
		<category android:name="android.intent.category.LAUNCHER" />
		</intent-filter>
		</activity>
	    <activity
	    android:name=".TwoActivity"
		android:noHistory="true"
		android:label="@string/app_name"
		android:exported="true">
	    </activity>
	</application>
	<uses-permission android:name="android.permission.VIBRATE"/>
</manifest>
```
##### MainActivity.java:
```java
  package com.myapplication.draw;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView; // Импортируем TextView
import android.app.Activity;
import android.view.ViewGroup;
import android.graphics.Color; // Импортируем класс Color
import android.widget.Toast;
import java.io.Serializable;
import java.util.HashMap;
import android.view.MotionEvent;

public class MainActivity extends Activity {
	private DrawGraphic drawView;
	private TextView directionTextView; // Поле для отображения направления
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main); // Устанавливаем макет активности
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.hide();
			
			// 2 СПОСОБА СОЗДАНИЯ ГРАФИКОВ - вручную и получение info от других activity
			
			// Получаем информацию о графике из другого activity
			Intent intent = getIntent();
			info i0 = (info) intent.getSerializableExtra("info");
			
			if (i0 == null) {
				// ВРУЧНУЮ ЗАПОЛНЯЕМ МАССИВЫ ДАННЫХ
				// Инициализация данных для графика
				HashMap<Integer, Double> timeData = new HashMap<>();
				HashMap<Integer, Double> data1 = new HashMap<>();
				HashMap<Integer, Double> data2 = new HashMap<>();
				HashMap<Integer, Double> data3 = new HashMap<>();
				
				// Заполнение данных для графика
				for (int i = 0; i <= 500; i++) {
					timeData.put(i, (double) (i / 50.0));
					data1.put(i, Math.sin(2 * 3.14 / 50 * i));
					data2.put(i, Math.cos(2 * 3.14 / 50 * i));
					data3.put(i, Math.sin(2 * 3.14 / 50 * i) * Math.cos(2 * 3.14 / 50 * i));
				}
				
				HashMap<Integer, Line> dat = new HashMap<>();
				Line l1 = new Line(timeData, data1, Color.RED, "График 1");
				Line l2 = new Line(timeData, data2, Color.BLUE, "График 2");
				Line l3 = new Line(timeData, data3, Color.GREEN, "График 3");
				dat.put(1, l1);
				dat.put(2, l2);
				dat.put(3, l3);
				
				// Создание графика
				drawView = new DrawGraphic(this, 1000, 800, 11, 11, 1, "Скорость", "Секунды", dat, 0);
				//drawView = new DrawGraphic(this, 1000, 800, 11, 11, 3, "Скорость", "Секунды", dat, 0, 11,3); // Делаем экран графика шире, даже если там нет данных
				//drawView.react = 0; // Заблокироаать график (нужно на моментах его заполнения)
				//drawView.setPaddMarg(60,40); // Увеличение отступов
			} else {
				// ВОЗВРАТ графика из другого activity
				drawView = new DrawGraphic(this, i0, 0);
			}
			
			// Добавление графика в контейнер
			((ViewGroup) findViewById(R.id.graph_view)).addView(drawView);
			
			// ДЛЯ ПЕРЕХОДА В ДРУГОЙ activity В ПОЛНОЭКРАННЫЙ РЕЖИМ
			// Обновление TextView после любого касания
			drawView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					drawView.onTouchEvent(event); // Обработка касания
					// Получаем флаг полноэкранного режима
					if (drawView.flagm == 1) {
						try {
							Intent intent = new Intent(MainActivity.this, TwoActivity.class);
							intent.putExtra("info", drawView.i);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
					return true;
				}
			});
			} catch (Exception e) {
			Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
}
```
##### TwoActivity.java:
```java
package com.myapplication.draw;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView; // Импортируем TextView
import android.app.Activity;
import android.view.ViewGroup;
import android.graphics.Color; // Импортируем класс Color
import android.widget.Toast;
import java.util.HashMap;
import android.view.MotionEvent;

public class TwoActivity extends Activity {
	DrawGraphic drawView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two); // Устанавливаем макет активности
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.hide();
		Intent intent = getIntent(); // Получаем информацию о графике
		info i = (info) intent.getSerializableExtra("info");
		
		drawView = new DrawGraphic(this, i, 1); // Из неё заполняем график
		
		// Добавление графика в контейнер
		((ViewGroup) findViewById(R.id.graph_view)).addView(drawView);
		
		drawView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				drawView.onTouchEvent(event); // Обработка касания
				// Получаем флаг закрытия полноэкранного режима
				if(drawView.flagm==1){ // Если нужно выйти из полноэкранного режима
					try{
						Intent intent = new Intent(TwoActivity.this, MainActivity.class);
						intent.putExtra("info", drawView.i);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					}
					catch(Exception e){
						Toast.makeText(TwoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
				return true;
			}
		});
	}
	
	
	@Override public void onBackPressed() {
		Intent intent = new Intent(TwoActivity.this, MainActivity.class);
		intent.putExtra("info", drawView.i);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
```
