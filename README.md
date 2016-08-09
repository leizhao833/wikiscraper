# wikiscraper

Maven build:

    mvn clean compile assembly:single

Find jar under target/

# Database authentication

Before run, put the database master key in ~/.auth/master

# Ubuntu 16.04 service setup

modify ExecStart in linux/wikiscraper.service
cp linux/wikiscraper.service /etc/systemd/system/
chmod a+x /etc/systemd/system/wikiscraper.service
cp linux/wikiscraper.sh $(ExecStart)




