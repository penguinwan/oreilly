# Requirement
- a valid login session id, refer to "How to find out session id"
- the url of start page of the ebook you would like to download, refer to "How to find out start page"

# Instruction
1. copy session id, and put it to `@Field String SESSIONID`
1. copy the url of start page, and put it to `@Field String STARTPAGE`
1. create directories in below structure to store downloaded file (note: due to the fact that linux or mac does not allow non-root program to create directory, you will need to do this step manually)
	```
		BASEDIR (your preferred name)
		  |- json
		  |- content
		  |- images
		  |- css
	```
1. copy the path of `BASEDIR`, and put it to `@Field String BASEDIR`
1. run `groovy all`, then files will be downloaded to `BASEDIR`
1. `BASEDIR/final.html` is the aggregated content of all chapters

# Additional information
1. configure the downloaded folder name by changing 
   - `@Field String DIR_JSON`
   - `@Field String DIR_CONTENT`
   - `@Field String DIR_IMAGE`
   - `@Field String DIR_CSS`

# How to find out session id
1. if you are using Chrome, login to oreilly learning page
1. after login >> View >> Developer >> Developer Tools
1. click at Application tab >> expand Cookies sidebar >> click `http://learning.oreilly...`
1. on the right panel, look for a cookie named `sessionid`, copy the value

# How to find out start page
1. go to oreilly learning page, open the ebook you would like to download
1. click at table of content, click at the first chapter, usually it is cover page
1. if you are using Chrome, right click the page >> View Page Source
1. search for `/api/`, copy the value

# Note
1. currently there is no throttle being implemented, meaning if the ebook is too large, the download might get blocked by oreilly