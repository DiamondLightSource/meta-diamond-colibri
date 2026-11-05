# Add the Diamond System Interface Card device tree
# (imx6ull-colibri-diamond-sic.dts/.dtsi + Makefile entry) to the kernel.
# Follows Toradex guide found here:
# https://developer.toradex.com/linux-bsp/os-development/build-yocto/custom-meta-layers-recipes-and-images-in-yocto-project-hello-world-examples/#add-a-custom-device-tree
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"


SRC_URI:append = " \
    file://${DIAMOND_DEVICETREE_SRC} \
    file://${DIAMOND_DEVICETREE_SRC}i \
"

do_configure:append() {
    cp ${WORKDIR}/${DIAMOND_DEVICETREE_SRC}* ${S}/arch/arm/boot/dts/nxp/imx/
}
