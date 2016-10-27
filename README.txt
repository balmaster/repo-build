Описание
Утилита автоматизирующая процесс раазработки проектов
	Имеющих компонентную организацию
	Использующие манифест в формате android repo для описания компонентов и определения релизов
	Использующиее maven сборщик для сборки компонентов

Базовое использование

Установка 
	
	Распаковать сборку repo-build-X.Y.Z.zip, например в ~/opt
	прописать путь в PATH

Получаем проект, переключаемся на релиз, генерируем build файл, собираем

	mk myproject
	cd myproject
	repo-build -M https://gitlab/myproject -b release1 init sync build-pom
	mvn clean install 
	
Переключаемся на фичу, генерируем build файл, собираем

	repo-build -f feature1 switch build-pom
	mvn clean install	
	
Описание возможностей

общие параметры
	-r <repo projects basedir>
		если не задан то равен текущему каталогу

команда build-pom для формирования build pom файла по манифесту
	-p <build pom file>
		если параметр не задан то считается равным <repo project basedir>/pom.xml

команда switch для переключения проектов
	на фича ветки
		-f <feature branch name>
	на релизные ветки по манифесту
		-m

команда prepare-merge для отведения prepareBuild веток и мержа в них фича веток
	-f <feature branch name>

команда export-bundles для формирования бандлов
	-t <target export directory>
		если не задана то равна bundles 
		бандлы выгружаются в виде
			<repository1>.bundle
			<repository2>.bundle
			bundles.xml
				файл реестра 
				<bundles>
					<bundle repository="" file="" branch="">
				</bundles> 
	по фича веткам
		-f <feature branch name>
	по релизным веткам по манифесту
		-m

команда init для выгрузки repo projects
	получает manifest проект если его нет 
	обновляет, если manifest проект уже есть 
	-M <manifet git url>
		требуется задавать если нет проекта manifest
	-b <manifest branch>
		параметр задает ветку манифеста которую надо использовать

команда sync для обновления repo projects
	получить обновление для проектов по манифесту из центрального репозитория		
	-j <count>
	-d отсоединится от локальных веток

команда status для получения git status по каждому компоненту

команда grep для поиска по всем компонентам силами git grep
    -e <expression> регулярное выражение для поиска

проект должен собираться в самодостаточный jar
	zip сборка
	скрипт для запуска под win
	скрипт для запуска под lin
	
	