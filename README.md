# meta-diamond-colibri

This repo contains two things:
- Yocto layers specific to Diamond's usage of the Colibri iMX6ULL deployed on the SIC+DPS Power Supply controllers
- The [KAS](https://kas.readthedocs.io/en/1.0/) build configuration to build the image

Between these two this one repo can build everything we need to build a complete OS for the Colbiri iMX6ULL.

# Build

```
uv venv
source .venv/bin/activate
uv pip install kas==5.5
kas-container build kas.yml
```

The build will take multiple hours on a fresh machine, and will use about 90GB of disk space.

## Restricting build resource usage

Add these lines to the `kas.yml` file in the `local_conf_header:` -> `meta-diamond-colibri: |` block:

```
    BB_NUMBER_THREADS = "1"
    PARALLEL_MAKE = "-j2"
    PARALLEL_MAKEINST = "-j2"
```

This will make the build only use a single core and two parallel Make tasks at once. This means you can still use the PC while a build is happening.

## Output files

When the build finishes the built files will be found in `build/tmp/deploy/images/colibri-imx6ull`. The important files are:

- `zImage`
- `imx6ull-colibri-diamond-sic.dtb`
- `u-boot-nand.imx`
- `colibri-imx6ull_Diamond-Image-upstream.ubifs`


# Installation onto iMX6ULL

## Using TFTP

- Put files into TFTP area: `cp zImage imx6ull-colibri-diamond-sic.dtb u-boot-nand.imx colibri-imx6ull_Diamond-Image-upstream.ubifs /dls_sw/work/tftpboot/dev/colibri-imx6ull/`
- `telnet` to the iMX6ULL, and get into UBoot prompt by interrupting autoboot
- Set up environment to point to tftp server:
```
env set serverip 172.23.247.36
env set ipaddr 172.23.240.226
env set netmask 255.255.255.0
env set gatewayip 172.23.240.254
env set server_prefix /colibri-imx6ull
env set fdtfile imx6ull-colibri-diamond-sic.dtb
env save
```
- `ubi part ubi`
- `tftpboot $loadaddr colibri-imx6ull/zImage`
- `ubi write $loadaddr kernel $filesize`
- `tftpboot $loadaddr colibri-imx6ull/colibri-imx6ull_Diamond-Image-upstream.ubifs`
- `ubi write $loadaddr rootfs $filesize`
- `tftpboot $loadaddr colibri-imx6ull/imx6ull-colibri-diamond-sic.dtb`
- `ubi write $loadaddr dtb $filesize`
- `tftpboot $loadaddr colibri-imx6ull/u-boot-nand.imx`
- `nand erase.part u-boot1`
- `nand write ${loadaddr} u-boot1 ${filesize}`
- `nand erase.part u-boot2`
- `nand write ${loadaddr} u-boot2 ${filesize}`

# Simple iMX6ULL Setup
A few instructions to give the Colibri enough network settings to function

hostnamectl hostname colibri17
connmanctl services
connmanctl config ethernet_00142d84bbbd_cable --domains diamond.ac.uk cs.diamond.ac.uk dc.diamond.ac.uk
connmanctl config ethernet_00142d84bbbd_cable --ipv4 manual 172.23.240.226 255.255.255.0 172.23.240.254
connmanctl config ethernet_00142d84bbbd_cable --nameservers 172.23.5.18 172.23.70.16 172.23.5.13
