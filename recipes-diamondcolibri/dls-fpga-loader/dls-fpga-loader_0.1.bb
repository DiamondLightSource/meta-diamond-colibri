SUMMARY = "Kernel module to load 7-series FPGAs"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit module

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " \
    file://dls_fpga_loader.c \
    file://Makefile \
"


S = "${WORKDIR}"
