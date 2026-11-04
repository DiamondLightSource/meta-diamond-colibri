# Add the Diamond System Interface Card device tree
# (imx6ull-colibri-diamond-sic.dts/.dtsi + Makefile entry) to the kernel.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"


SRC_URI:append = " \
    file://0001-ARM-dts-imx6ull-colibri-add-Diamond-SIC.patch \
    file://imx6ull-colibri-diamond-sic.dts \
    file://imx6ull-colibri-diamond-sic.dtsi \
"

do_configure:append() {
    cp ${WORKDIR}/imx6ull-colibri-diamond-sic.dts* ${S}/arch/arm/boot/dts/nxp/imx/
}
