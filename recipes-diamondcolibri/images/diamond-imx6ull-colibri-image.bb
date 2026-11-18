inherit core-image

SUMMARY = "Diamond Power Supplies iMX6ULL OS"
DESCRIPTION = "Configured for use with Power Supplies SIC and DPS FPGAs"
LICENSE = "MIT"

export IMAGE_BASENAME = "Diamond-Image"
MACHINE_NAME ?= "${MACHINE}"
IMAGE_NAME = "${MACHINE_NAME}_${IMAGE_BASENAME}"

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

ROOTFS_POSTPROCESS_COMMAND += " add_rootfs_version; add_home_root_symlink;"

IMAGE_LINGUAS = "en-us"

CONMANPKGS ?= "connman connman-plugin-loopback connman-plugin-ethernet connman-client"


EXTRA_IMAGE_FEATURES = "allow-empty-password empty-root-password allow-root-login"

IMAGE_INSTALL += " \
    packagegroup-boot \
    packagegroup-basic \
    packagegroup-base-tdx-cli \
    packagegroup-machine-tdx-cli \
    udev-extraconf \
    ${CONMANPKGS} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'timestamp-service systemd-analyze', '', d)} \
    i2c-tools \
    dls-fpga-loader \
"
# kernel-module-dls-fpga-loader

IMAGE_FSTYPES = "ubifs"
