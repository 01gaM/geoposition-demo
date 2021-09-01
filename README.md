# Подключаемый модуль определения и обработки данных о местоположении мобильного устройства для платформы Android

Gradle-модуль для определения геопозиции устройства, включает в себя следующие функции: 
- Получение последней доступной координаты (используя Fused Location Provider)
- Запрос текущих координат (не использует кэш, больше время ожидания, свежие данные)
- Подписка на обновление координат с заданным интервалом в минутах

Геолокационный модуль использует библиотеку **«play-services-location»**, которая взаимодействует с сервисами Google Play (используется **Fused Location Provider API**). Также используется стандартный **Android Location API** и модуль поддерживается на мобильных устройствах без сервисов Google Play.

## Подключение модуля
В файле `settings.gradle` необходимо добавить следующую зависимость:
```
include ':geolocationmodule'
```

## Использование модуля
Для использования функций модуля необходимо создать экземпляр класса LocationSupplier, передав ему контекст.
#### Пример:
```java
LocationSupplier locationSupplier = new LocationSupplier(CurrCoordinatesActivity.this);
```
Объект класса LocationSupplier имеет следующие доступные методы, описанные в интерфейсе [ILocationSupplier](https://github.com/01gaM/geoposition-demo/blob/master/geolocationmodule/src/main/java/com/example/geolocationmodule/ILocationSupplier.java):

- ### Получение последней доступной координаты
```java
  void getLastKnownLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException;
```

#### Пример:
```java
  locationSupplier.getLastKnownLocation(myCallback);
```

- ### Запрос текущих координат
```java
   void requestCurrentLocation(ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException,
            AirplaneModeOnException, DeviceLocationDisabledException;
```
#### Пример:
```java
  locationSupplier.requestCurrentLocation(myCallback);
```

Отменить запрос на получение текущих координат можно с помощью следующей функции:
```java
void cancelCurrentLocationRequest();
```


- ### Обновление координат с заданным интервалом в минутах
```java
   void requestLocationUpdates(double intervalMin, ILocationCallback callback) throws LocationPermissionNotGrantedException, LocationProviderDisabledException,
            IntervalValueOutOfRangeException, DeviceLocationDisabledException, AirplaneModeOnException;
```

#### Пример:
```java
  locationSupplier.requestLocationUpdates(interval, myCallback);
```

Отменить запрос на обновление координат можно помощью следующей функции:
```java
void stopLocationUpdates();
```

- ### Выбор режима определения координат
```java
void setAccuracyPriority(AccuracyPriority accuracyPriority);
```
Метод позволяет задать режим определения координат, для текущего экземпляра класса "LocationSupplier". На вход принимается одно из допустимых значений в перечислении ["AccuracyPriority"](https://github.com/01gaM/geoposition-demo/blob/master/geolocationmodule/src/main/java/com/example/geolocationmodule/AccuracyPriority.java):
AccuracyPriority – перечисление, содержащее поддерживаемые в [Fused Location Provider API](https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest) уровни приоритета запросов на получение данных о местоположении. Приоритет задает желаемую точность результата и расход заряда батареи:
- **PRIORITY_HIGH_ACCURACY** – используется для запроса наиболее точных доступных данных о местоположении, требует наибольшего расхода заряда батареи мобильного устройства.
- **PRIORITY_BALANCED_POWER_ACCURACY** – используется для запроса данных о местоположении с точностью до квартала (около 100 метров), требует среднего расхода заряда батареи мобильного устройства.
- **PRIORITY_LOW_POWER** – используется для запроса данных о местоположении с точностью до города (около 10 километров), требует низкого расхода заряда батареи мобильного устройства.
Значение по умолчанию - PRIORITY_HIGH_ACCURACY.

## Разрешения
Для работы методов «getLastKnownLocation», «requestCurrentLocation» и «requestLocationUpdates» необходимо одно из следующих разрешений:
- **ACCESS_FINE_LOCATION** - позволяет приложению получать
данные о точном местоположении устройства;
- **ACCESS_COARSE_LOCATION** - позволяет приложению получать
данные о приблизительном местоположении устройства;
В случае, если оба разрешения отсутствуют, эти методы вызывают исключение «LocationPermissionNotGrantedException».


## Страница на Notion
https://www.notion.so/3e9c6602b4894615bacaad862874a19b
### UML диаграммы
https://www.notion.so/UML-223f73ec3c9342c2b6f4943463acea49
