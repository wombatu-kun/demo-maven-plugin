# demo-maven-plugin

## Что он делает:  
Плагин для автоматизации процесса  развёртывания/обновления демонстраций.
Колбасить вручную - во-первых, отнимает некоторое время, во-вторых, быстро надоедает, в-третьих, чревато косяками типа: 
- грохнул файлы, не прибив старый процесс;
- запустил новую демку у себя на sshfs, вместо удалёнки;
- забыл отмонтировать sshfs;
- все прочие косяки, которые можно натворить, ковыряясь руками в командной строке линупса.

Плагин избавляет от этого еррорпрона и берёт на себя:
1) заливку дистрибутива на демо-комп;
2) остановку старой версии приложения;
3) выпиливание старых файлов и распаковку на их место содержимого архива-дистрибутива новой версии;
4) запуск обновлённой версии демки.

## Как установить:
1) Склонировать себе репозиторий;
2) Выполнить там: mvn install;
3) Создать/дописать в ~/.m2/settings.xml такое вот:  
```xml
<settings>
  <pluginGroups>
    <pluginGroup>wombatukun.plugins</pluginGroup>
  </pluginGroups>
</settings>
```  
*3й шаг можно не выполнять, но тогда каждый раз запускать плагин придётся как-то так:  
mvn wombatukun.plugins:demo-maven-plugin:1.0-SNAPSHOT:deploy  
вместо лапидарного: mvn demo:deploy

## Как пользоваться:  
### Требования к проекту и настройка:
1) maven project;
2) дистрибутив приложения должен быть упакован в один zip-архив, все файлы должны лежать сразу в корне архива;
3) для настройки плагина нужно в главном pom'е своего проекта задать конфигурацию плагина подобным образом:  
```xml
<build>
    <plugins>
        <!-- .... -->
        <plugin>
                <groupId>wombatukun.plugins</groupId>
                <artifactId>demo-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <configuration>
                    <host>ip-address</host>
                    <port>22</port>
                    <user>admin-login</user>
                    <pass>admin-pass</pass>
                    <directory>remote-dir</directory>
                    <archive>target/blah-blah-blah-build-1.0-SNAPSHOT-bin.zip</archive>
                    <stop>./stop.sh</stop>
                    <start>./start.sh</start>
                </configuration>
            </plugin>
    </plugins>
</build>
```  
*start/stop - не обязательно должны быть скриптами, можно и просто команды задать.

### Запуск плагина:  
Просто остановить демку, работающую удалённо: mvn demo:stop  
Просто залить на демо-комп новый дистрибутив: mvn demo:upload  
Ваще всё сразу замутить: mvn demo:deploy
