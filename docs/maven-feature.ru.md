# команды
## maven-feature-update-parent

Команда используется для обновления версии центрального parent компонента для компонентов имеющих фичабранчи
Версия parent обновляется только при условии что компонент наследуется от центрального parent
изменения коммитятся в фича бранчи с комментарием vup parent to <version>
Параметры

    -f фичабранч
    -P parent

Перед пременение команды необходимо выполнить команды sync switch
При выполнении команд группы maven-feature-..., Maven запускается с помощью [maven-invoker API](http://maven.apache.org/shared/maven-invoker/)
требуется задать переменную окружения ```M2_HOME``` 


## feature-update-versions
Команда используется для обновления свойств задающих версии для зависимостей, для компонентов имеющих фичабранчи
Перед обновлением версий, строится ациклический граф зависимостей компонентов без учета версий
затем этот граф топологически сортируется по зависимостям
затем для отсортированных компонентов последовательно выполняются команды


    mvn versions:update-versions -Dincludes=$includes
    mvn clean install -DskipTests

затем, если pom.xml изменен

    git add pom.xml
    git commit -m 'update dependencies to last feature snapshot'

таким образом в локальном репозитории будут собраны корректные артефакты, кторые будут 
учитываться version plugin


### Подерживаемые способы задания зависимостей между модулями
Модули внутри одного помпонента организованы в структуру parent -> modules стандартную для Maven
Для задания зависимостей от модулей других компонентов зависмости нужно задать в dependencyManagement в корневом модуле компонента
тогда они будут унаследованы другими модулями компонента

Для задания версии поддерживаются два варианта:
Первый вариант - явно указывать версию в зависимости 
    
    <dependency>
      <groupId>some.group</groupId>
      <artifactId>some.artifact</artifactId>
      <version>some.version</version>
    </dependency>

в этом случае для управления зависимостями будут использоваться следующие goals maven-version-plugin:
* use-last-versions 

при использовании use-last-versions важно чтобы <groupId> не модержал подстановочных переменных, например ${project.groupId}

Второй вариант - использовать свойства для задания версий

    <properties>
      <some.version>some.version</some.version>
    </properties    
    
    <dependency>
      <groupId>some.group</groupId>
      <artifactId>some.artifact</artifactId>
      <version>${some.version}</version>
    </dependency>
    
в этом случае для для управления зависимостями будут использоваться следующие goals maven-version-plugin:
* update-properties

Важно: при использовании update-properties требуется чтобы свойства были объявлены в томже модуле что и зависимости 

# Поддержка общей сборки релиза 
## build-pom 
Команда для формирования build pom файла по манифесту
    
    -p <build pom file>
		если параметр не задан то считается равным <repo project basedir>/pom.xml
