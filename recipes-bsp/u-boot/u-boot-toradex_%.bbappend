FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Increase autoboot delay from the defconfig default of 1 second
SRC_URI += "file://bootdelay.cfg"

do_configure:append() {
    # Remove existing fdtfile, if present
    sed -i '/"fdtfile=.*\\0" \\/d' ${S}/include/configs/colibri-imx6ull.h

    # Add custom fdtfile
    sed -i 's/\("fdt_board=.*\\0" \\\)/\0\n      "fdtfile=${DIAMOND_DEVICETREE_BIN}\\0" \\/' ${S}/include/configs/colibri-imx6ull.h
}

do_deploy:append() {
    install -D -m 644 ${B}/u-boot-nand.imx ${DEPLOYDIR}/u-boot-nand-${MACHINE}-${PV}-${PR}.imx
    cd ${DEPLOYDIR}
    ln -sf u-boot-nand-${MACHINE}-${PV}-${PR}.imx u-boot-nand-${MACHINE}.imx
    ln -sf u-boot-nand-${MACHINE}-${PV}-${PR}.imx u-boot-nand.imx
}
