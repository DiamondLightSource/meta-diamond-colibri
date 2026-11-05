do_configure:append() {
    # Remove existing fdtfile, if present
    sed -i '/"fdtfile=.*\\0" \\/d' ${S}/include/configs/colibri-imx6ull.h

    # Add custom fdtfile
    sed -i 's/\("fdt_board=.*\\0" \\\)/\0\n      "fdtfile=${DIAMOND_DEVICETREE_BIN}\\0" \\/' ${S}/include/configs/colibri-imx6ull.h
}
