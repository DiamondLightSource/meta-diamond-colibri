inherit core-image

SUMMARY = "Diamond Power Supplies iMX6ULL OS"
DESCRIPTION = "Configured for use with Power Supplies SIC and DPS FPGAs"

LICENSE = "MIT"

MACHINE = "colibri-imx6ull"

#Prefix to the resulting deployable tarball name
export IMAGE_BASENAME = "Diamond-Image"
MACHINE_NAME ?= "${MACHINE}"
IMAGE_NAME = "${MACHINE_NAME}_${IMAGE_BASENAME}"

# Copy Licenses to image /usr/share/common-license
COPY_LIC_MANIFEST ?= "1"
COPY_LIC_DIRS ?= "1"

add_rootfs_version () {
    printf "${DISTRO_NAME} ${DISTRO_VERSION} (${DISTRO_CODENAME}) \\\n \\\l\n" > ${IMAGE_ROOTFS}/etc/issue
    printf "${DISTRO_NAME} ${DISTRO_VERSION} (${DISTRO_CODENAME}) %%h\n" > ${IMAGE_ROOTFS}/etc/issue.net
    printf "${IMAGE_NAME}\n\n" >> ${IMAGE_ROOTFS}/etc/issue
    printf "${IMAGE_NAME}\n\n" >> ${IMAGE_ROOTFS}/etc/issue.net
}

add_home_root_symlink () {
    ln -sf ${ROOT_HOME} ${IMAGE_ROOTFS}/home/root
}

# add the rootfs version to the welcome banner
ROOTFS_POSTPROCESS_COMMAND += " add_rootfs_version; add_home_root_symlink;"

IMAGE_LINGUAS = "en-us"
#IMAGE_LINGUAS = "de-de fr-fr en-gb en-us pt-br es-es kn-in ml-in ta-in"

CONMANPKGS ?= "connman connman-plugin-loopback connman-plugin-ethernet connman-client"

IMAGE_INSTALL += " \
    packagegroup-boot \
    packagegroup-basic \
    packagegroup-base-tdx-cli \
    packagegroup-machine-tdx-cli \
    udev-extraconf \
    ${CONMANPKGS} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'timestamp-service systemd-analyze', '', d)} \
    i2c-tools \
"

MACHINE_EXTRA_RDEPENDS += "kernel-module-dls-fpga-loader"

IMAGE_FSTYPES = "ubifs"
# These parameters were discovered by either the `nand` or `ubi` commands in the Uboot environment of an imx6ull.
# Note the -e parameter value was "131072", but when trying to load the OS a kernel panic triggered, due to the
# system saying the actual size of the block was "126976".
MKUBIFS_ARGS = "-m 2048 -e 126976 -c 4057"

# We don't need any of these modules that are by default included in the "minimal" image
MACHINE_FEATURES:remove ="usbgadget usbhost alsa touchscreen screen screen usbgadget usbhost ext2 alsa touchscreen wifi bluetooth 3g "
# Nor do we need any of these features
DISTRO_FEATURES:remove = "alsa bluetooth debuginfod ext2 pcmcia usbgadget usbhost wifi zeroconf 3g nfc x11 seccomp opengl multiarch wayland vulkan pam tpm2 opengl wayland x11 polkit pulseaudio gobject-introspection-data"

ACCEPT_FSL_EULA = "1"
