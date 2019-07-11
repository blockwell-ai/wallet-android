.PHONY: build update-server

build:
	@node scripts/build.js

config:
	@node scripts/config.js && node scripts/build.js

update-server:
	@rsync -avz \
		--exclude-from=.exclude \
		. app@android.apiminer.com:~/qr-android

update-deployer:
	@rsync -avz \
		--exclude-from=.exclude \
		. app@qr.lenswallet.io:~/android
