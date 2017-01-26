# команды
## maven-feature-update-parent

## maven-feature-update-versionsAZ

# Подерживаемые способы задания зависимостей между модулями
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