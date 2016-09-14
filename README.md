# wikiscraper

Maven build:

    mvn clean compile assembly:single

Find jar under target/

	cp target/xxx.jar $(path in wikiscraper.sh)

# Database authentication

Before run, put the database master key in ~/.auth/master

# Temp data

Log: ~/.wikitemp/log/wikiscraper-log.%u.%g.log

Debug: upon parsing failure of a change page, the raw html 
can be found at ~/.wikitemp/html/changePage_yyyy_MM_dd_HH_mm_ss.html

# Ubuntu 16.04 service setup

	modify ExecStart in linux/wikiscraper.service
	cp linux/wikiscraper.service /etc/systemd/system/
	chmod a+x /etc/systemd/system/wikiscraper.service
	cp linux/wikiscraper.sh $(ExecStart)
	chmod a+x $(ExecStart)

# Ubuntu 16.04 service operations
	sudo systemctl start wikiscraper.service
	sudo systemctl status wikiscraper.service
	sudo systemctl stop wikiscraper.service



