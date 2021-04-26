@rem Gets the current dir with forward slashes because thats what docker needs (eg: /c//users/document/thisdir/)
@set "dir=/%~dp0"
@set "dir=%dir:\=/%"
@set "dir=%dir::=/%"
@echo Converted dir: "%dir%"

docker run --name database1 --mount type=bind,source="%dir%"data,target=/var/lib/postgresql/data -e POSTGRES_PASSWORD=password -p 5432:5432 postdb