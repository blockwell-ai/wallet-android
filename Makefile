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


blockwell:
	./gradlew installRelease -PAPP_ID=blockwell \
		-Pcolor.toolbar=#020a1e \
		-Pcolor.toolbarText=#ffffff \
		-Pcolor.toolbarTextGreyed=#7e7e7e \
		-Pcolor.primary=#004fff \
		-Pcolor.primaryText=#ffffff \
		-Pcolor.primaryTextGreyed=#d8d8d8 \
		-Pcolor.contentBackground=#ffffff \
		-Pcolor.colorText=#797979 \
		-Pcolor.colorTextEmphasis=#000000 \
		-Pcolor.faintBackground=#fafaff \
		-Pcolor.colorHelper=#b7b7b7 \
		-Pcolor.link=#004fff \
		-Pcolor.success=#558B2F \
		-Pcolor.error=#CDAF3823 \
		-Pstring.app_name="Blockwell Wallet"