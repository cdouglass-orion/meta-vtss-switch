DESCRIPTION = "Lightweight secure web server"
HOMEPAGE = "http://www.hiawatha-webserver.org"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://LICENSE;md5=751419260aa954499f7abaabaa882bbe"
DEPENDS = "libxml2 libxslt"

SECTION = "net"

PR = "r3"

SRC_URI = "http://hiawatha-webserver.org/files/${PN}-${PV}.tar.gz \
           file://smbstax-json.conf \
           file://hiawatha-init \
           file://hiawatha.service "

SRC_URI[md5sum] = "8abc4f85dbb9a76ed66e7f35de520064"
SRC_URI[sha256sum] = "5e40119afb050b11737250c08d89ac7ba7472645738a48c06aa79979a19729fc"

INITSCRIPT_NAME = "hiawatha"
INITSCRIPT_PARAMS = "defaults 70"

SYSTEMD_SERVICE_${PN} = "hiawatha.service"

inherit cmake update-rc.d systemd

EXTRA_OECMAKE = " -DENABLE_IPV6=OFF \
                  -DENABLE_CACHE=OFF \
                  -DENABLE_DEBUG=OFF \
                  -DENABLE_SSL=OFF \
                  -DENABLE_TOOLKIT=OFF \
                  -DENABLE_CHROOT=OFF \
                  -DENABLE_XSLT=ON \
                  -DENABLE_TOMAHAWK=OFF \
                  -DCMAKE_INSTALL_MANDIR=${mandir} \
                  -DCMAKE_INSTALL_BINDIR=${bindir} \
                  -DCMAKE_INSTALL_SBINDIR=${sbindir} \
                  -DCMAKE_INSTALL_SYSCONFDIR=${sysconfdir} \
                  -DCMAKE_INSTALL_LIBDIR=${libdir} \
                  -DLOG_DIR=/var/log/hiawatha \
                  -DPID_DIR=/var/run \
                  -DWEBROOT_DIR=/var/www/hiawatha \
                  -DWORK_DIR=/var/lib/hiawatha "

do_install_append() {
    # Copy over init script and sed in the correct sbin/log path
    sed -i \
        -e 's,sed_sbin_path,${sbindir},' \
        -e 's,sed_localstatedir,${localstatedir},' \
        ${WORKDIR}/hiawatha-init
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/hiawatha-init ${D}${sysconfdir}/init.d/hiawatha
    install -d ${D}${sysconfdir}/hiawatha
    install -m 0644 ${WORKDIR}/smbstax-json.conf ${D}${sysconfdir}/hiawatha/hiawatha.conf

    if ${@base_contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}/${systemd_unitdir}/system
        install -m 644 ${WORKDIR}/hiawatha.service ${D}/${systemd_unitdir}/system
    fi

}

CONFFILES_${PN} = " \
    ${sysconfdir}/hiawatha/cgi-wrapper.conf \
    ${sysconfdir}/hiawatha/hiawatha.conf \
    ${sysconfdir}/hiawatha/index.xslt \
    ${sysconfdir}/hiawatha/mimetype.conf \
"
