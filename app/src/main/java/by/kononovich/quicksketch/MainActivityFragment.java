package by.kononovich.quicksketch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivityFragment extends Fragment {
    private QuickSketchView quickSketchView;  // Обработка событий касания и рисования
    private float acceleration;          // используются для пересчета изменений в
    private float currentAcceleration;   //  ускорении устройства
    private float lastAcceleration;    // для выявления события встряхивания
    private boolean dialogOnScreen = false; // для предотвращения одновременного появления нескольких диалоговых окон

    // Используется для обнаружения встряхивания устройства (средняя величина для многих устройств)
    private static final int ACCELERATION_THRESHOLD = 100000;

    // Используется для идентификации запросов на использование
    // внешнего хранилища; необходимо для работы функции сохранения
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true); // у фрагмента имеются команды меню

        quickSketchView = (QuickSketchView) view.findViewById(R.id.sketchView);

        // Инициализация параметров ускорения
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        return view;
    }

    // Начало прослушивания событий датчика
    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening(); // Прослушивание события встряхивания
    }

    // Прослушивание события встряхивания
    private void enableAccelerometerListening() {
        // Получение объекта SensorManager
        SensorManager sensorManager = (SensorManager) getActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        // Регистрация для прослушивания событий акселерометра
        sensorManager.registerListener(sensorEventListener,    // объект, реагирующий на события
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   // определяем тип датчика
                SensorManager.SENSOR_DELAY_NORMAL);   // опеределяем частоту получения событий датчика (по умолчанию)
    }

    // Прекращение прослушивания событий акселерометра
    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening();  // Прекращение прослушивания
    }

    private void disableAccelerometerListening() {

        SensorManager sensorManager = (SensorManager) getActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        // Прекращение прослушивания событий акселерометра
        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    // Обработчик для событий акселерометра
    private final SensorEventListener sensorEventListener =
            new SensorEventListener() {
                // Проверка встряхивания по показаниям акселерометра
                @Override
                public void onSensorChanged(SensorEvent event) {
                    // Проверяем нет ли на экране других диалоговых окон
                    if (!dialogOnScreen) {
                        // Получить значения x, y и z для SensorEvent
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        // Сохранить предыдущие данные ускорения
                        lastAcceleration = currentAcceleration;

                        // Вычислить текущее ускорение в м/с2
                        // в направлениях x (влево/вправо), y (вверх/ вниз) и z (вперед/назад)
                        currentAcceleration = x * x + y * y + z * z;

                        // Вычислить изменение ускорения
                        acceleration = currentAcceleration *
                                (currentAcceleration - lastAcceleration);

                        // Если изменение превышает заданный порог - пользователь перемещает устройство
                        // достаточно быстро и перемещение может интерпретироваться как встряхивание.
                        if (acceleration > ACCELERATION_THRESHOLD)
                            confirmErase();
                    }
                }

                // Обязательный метод интерфейса SensorEventListener  - без изменений
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };

    // Подтверждение стирания рисунка
    private void confirmErase() {
        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
        fragment.show(getFragmentManager(), "erase dialog");
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sketch_fragment_menu, menu);
    }

    // Обработка выбора команд меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialog = new ColorDialogFragment();
                colorDialog.show(getFragmentManager(), "color dialog");
                return true;
            case R.id.line_width:
                LineWidthDialogFragment widthDialog =
                        new LineWidthDialogFragment();
                widthDialog.show(getFragmentManager(), "line width dialog");
                return true;
            case R.id.delete_drawing:
                confirmErase();  // Получить подтверждение перед стиранием
                return true;
            case R.id.save:
                savePicture();  // Проверить разрешение и сохранить рисунок
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // При необходимости метод запрашивает разрешение
    // или сохраняет изображение, если разрешение уже имеется
    private void savePicture() {
        // Проверить, есть ли у приложения разрешение,
        // необходимое для сохранения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {

                // Объяснить, почему понадобилось разрешение
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(getActivity());

                    // Назначить сообщение AlertDialog
                    builder.setMessage(R.string.permission_explanation);

                    // Добавить кнопку OK в диалоговое окно
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Запросить разрешение
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                                }
                            }
                    );

                    // Отображение диалогового окна
                    builder.create().show();
                } else {
                    // Запросить разрешение
                    requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                }
            } else {  // Если разрешение уже имеет разрешение для записи
                quickSketchView.saveImage();  // Сохранить изображение
            }
        } else {
            quickSketchView.saveImage();
        }

    }

    // Вызывается системой, когда пользователь предоставляет
    // или отклоняет разрешение для сохранения изображения
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        // выбираем действие в зависимости от того,
        // какое разрешение было запрошено
        switch (requestCode) {
            case SAVE_IMAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    quickSketchView.saveImage();
                return;
        }
    }


    public QuickSketchView getQuickSketchView() {
        return quickSketchView;
    }

    // Проверяет, отображается ли диалоговое окно
    public void setDialogOnScreen(boolean visible) {
        dialogOnScreen = visible;
    }
}


