@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.6')
@Grab('commons-io:commons-io:2.6')

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset
import groovy.json.JsonSlurper
import org.apache.http.impl.cookie.BasicClientCookie
import groovy.io.FileType
import groovy.transform.Field


@Field String BASEDIR = '/Users/ebook'
@Field String STARTPAGE = 'https://learning.oreilly.com/api/v1/book/...'
@Field String DIR_JSON = 'json'
@Field String DIR_CONTENT = 'content'
@Field String DIR_IMAGE = 'images'
@Field String DIR_CSS = 'css'

def filename(def url) {
	def name = url.substring(url.lastIndexOf('/')+1, url.size())
	return name
}

def extension(def filename) {
	def extension = filename.substring(filename.lastIndexOf('.')+1, filename.size())
	return extension
}

def stringFromStream(def stream) {
	StringWriter writer = new StringWriter()
	IOUtils.copy(stream, writer, Charset.forName('UTF-8'))
	writer.toString()
}

def cookieMonster(def http) {
	addCookie(http, [domain:'learning.oreilly.com', path:'/', name:'sessionid', value:'xxx'])
	addCookie(http, [domain:'learning.oreilly.com', path:'/', name:'csrfsafari', value:'xxx'])
	addCookie(http, [domain:'learning.oreilly.com', path:'/', name:'BrowserCookie', value:'xxx'])

	addCookie(http, [domain:'.oreilly.com', path:'/', name:'logged_in', value:'y'])
	addCookie(http, [domain:'.oreilly.com', path:'/', name:'orm-jwt', value:'xxx'])
}

def link(def url, Closure handler) {
	def http = new HTTPBuilder(url)
	cookieMonster(http)

	http.parser.defaultParser = { resp ->
		handler.call(url, resp.entity.content)
		return ''
	}
	http.parser.'text/html' = { resp ->
		def html = stringFromStream(resp.entity.content)

		handler.call(url, html)
		
		return html
	}
	http.parser.'text/xhtml' = { resp ->
		def xhtml = stringFromStream(resp.entity.content)

		handler.call(url, xhtml)
		
		return xhtml
	}
	http.parser.'application/json' = { resp ->
		def json = stringFromStream(resp.entity.content)

		handler.call(url, json)

		return json
	}

	def result
	http.request(Method.GET, '*/*') {
 		response.success = { resp, content ->
  			result = content
 		}
	}

	result
}

void addCookie(def http, def m) {
    // create the basic cookie object
    def cookie = new BasicClientCookie(m.name, m.value)
    // add optional cookie properties
    m.findAll { k,v -> !(k in ['name', 'value']) }.
        each { k,v -> cookie[k] = v }
    // add the new cookie to the client's cookie-store
    http.client.cookieStore.addCookie cookie
}

def saveStreamTo(def dir) {
	{ url, os ->
		def path = "${BASEDIR}/${dir}/${filename(url)}"
		new File(path).withOutputStream { out ->
			out << os
		}
	}
}

def saveStringTo(def dir) {
	{ url, content ->
		new File("${BASEDIR}/${dir}/${filename(url)}") << content
	}
}

def init(def url, Closure... visitors) {
	println "navigating ${url}"
	def jsonSlurper = new JsonSlurper()
	def landingPage = link(url, saveStringTo(DIR_JSON))
	def root = jsonSlurper.parseText(landingPage)

	root.chapters.each {
		def chapter = link(it, saveStringTo(DIR_JSON))
		def chapterJson = jsonSlurper.parseText(chapter)
		visitors.each {
			it(chapterJson)
		}
	}
}

def imageDownloader = { root ->
	def baseUrl = root.asset_base_url
	root.images.each {
		println "downloading image: ${baseUrl}${it}"
		link(baseUrl + it, saveStreamTo(DIR_IMAGE))
	}
}


def contentDownloader = { root ->
	println "downloading content: ${root.content}"
	def content = link(root.content, saveStringTo(DIR_CONTENT))
	def extension = extension(filename(root.content))
	new File("${BASEDIR}/final.${extension}").withWriterAppend { out ->
		out.println content
	}
}


def cssDownloader = { root ->
	root.stylesheets.each { it ->
		def filename = filename(it.original_url)
		if (!new File("${BASEDIR}/${DIR_CSS}/${filename}").exists()) {
			println "downloading css: ${it.url}"
			link(it.original_url, saveStreamTo(DIR_CSS))
		}
	}
}

init(STARTPAGE, imageDownloader, contentDownloader, cssDownloader)