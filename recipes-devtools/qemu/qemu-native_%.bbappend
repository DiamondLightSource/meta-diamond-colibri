FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
                file://0001-Hack-for-RHEL-not-allowing-to-mmap-low-addresses.patch \
            "
