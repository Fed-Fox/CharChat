# О программе

Программа представляет из себя школьный проект для изучения серверной и клиентской части мессенджеров.

# Запуск

Для запуска необходима версия java 21 и выше

Стандартный запуск (Сервер откроется на порте 8080 с бд на postegresql)

> java -jar <файл с расширением> --spring.datasource.url=<адрес с базе данных> --spring.datasource.username=<имя пользователя в базе данных>  --spring.datasource.password=<пароль пользователя>

Запуск с натройкой некоторых частей программы

> java -jar <файл с расширением> --server.port=<порт> --spring.datasource.url=<адрес с базе данных> --spring.datasource.username=<имя пользователя в базе данных>  --spring.datasource.password=<пароль пользователя> --spring.datasource.driver-class-name=<драйвер для базы данных>
