# meta-diamond-colibri

This repo contains two things:
- Yocto layers specific to Diamond's usage of the Colibri iMX6ULL deployed on the SIC+DPS Power Supply controllers
- The [KAS](https://kas.readthedocs.io/en/latest/) build configuration to build the image

Between these two this one repo can build everything we need to build a complete OS for the Colibri iMX6ULL.

# Prerequisites

- **docker or podman** on the host. `kas-container` runs the whole build inside a container and will fail with
  `no container engine found, need docker or podman` without one.
- [uv](https://docs.astral.sh/uv/) to install kas.
- About 90GB of free disk space.

Alternatively, open the repo in the provided devcontainer (`.devcontainer/`). Its `postCreate.sh` creates the
virtualenv and installs kas automatically.

Note that the build runs inside the kas container, so the host OS mostly doesn't matter. Bitbake may warn that the
host distribution "has not been validated"; this can be ignored. `recipes-devtools/qemu` carries a patch needed
when building on RHEL hosts.

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

## Day-to-day development

Get an interactive bitbake shell inside the build container:

```
kas-container shell kas.yml
```

From that shell you can work on individual recipes, e.g. to force a clean rebuild of U-Boot:

```
bitbake -c cleansstate u-boot-toradex
bitbake u-boot-toradex
```

To inspect the final value of a variable: `bitbake-getvar -r <recipe> <VARIABLE>`.

All upstream layers are pinned to specific commits in `kas.yml`. To update a layer, change its `commit:` value
and rebuild.

# Repo layout: where to change what

| What | Where |
|---|---|
| Image contents and filesystem type | `recipes-diamondcolibri/images/diamond-imx6ull-colibri-image.bb` |
| Custom device tree (SIC board) | `recipes-kernel/linux/files/imx6ull-colibri-diamond-sic.dts(i)` |
| Adding the device tree to the kernel build | `recipes-kernel/linux/linux-toradex-upstream_%.bbappend` |
| FPGA loader driver for the SIC/DPS FPGAs | `recipes-diamondcolibri/dls-fpga-loader/` |
| U-Boot tweaks (default `fdtfile`, `bootdelay`, NAND image deploy) | `recipes-bsp/u-boot/` |
| Machine/distro feature trimming, device tree name, UBIFS parameters | `conf/machine/colibri-imx6ull-extra.conf` |
| Pinned upstream layer versions, machine, distro, kernel version | `kas.yml` |

**Note:** `conf/machine/colibri-imx6ull-extra.conf` is *not* a separate machine. The machine is still
`colibri-imx6ull`; this file is pulled in by the `include` line at the bottom of `conf/layer.conf`, so its
settings apply to every build that uses this layer.



# Default login

The image is built with `allow-empty-password empty-root-password allow-root-login`
(see `EXTRA_IMAGE_FEATURES` in the image recipe), so it is possible to log in as `root` with **no password**.

This is purely for use while testing and should be changed once we get closer to production.

# Installation onto iMX6ULL

## Using TFTP

These steps assume the module has already been partitioned with the `u-boot1`, `u-boot2` and `ubi` NAND
partitions, and that the `ubi` partition contains `kernel`, `rootfs` and `dtb` volumes (as set up by the Toradex
BSP).

The IP addresses below are examples for one particular board and TFTP server; substitute your own.

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
- Write the kernel, rootfs, device tree and U-Boot:
```
ubi part ubi
tftpboot $loadaddr colibri-imx6ull/zImage
ubi write $loadaddr kernel $filesize
tftpboot $loadaddr colibri-imx6ull/colibri-imx6ull_Diamond-Image-upstream.ubifs
ubi write $loadaddr rootfs $filesize
tftpboot $loadaddr colibri-imx6ull/imx6ull-colibri-diamond-sic.dtb
ubi write $loadaddr dtb $filesize
tftpboot $loadaddr colibri-imx6ull/u-boot-nand.imx
nand erase.part u-boot1
nand write ${loadaddr} u-boot1 ${filesize}
nand erase.part u-boot2
nand write ${loadaddr} u-boot2 ${filesize}
```

## Recovery

If you wrote a bad u-boot and the Colibri module no longer boots, it can be recovered.
Follow the guide from [Toradex here](https://developer.toradex.com/easy-installer/toradex-easy-installer/loading-toradex-easy-installer/)

Short version:
- Download a Tezi version for the Colibri iMX6ULL (tested with version 7.8.0)
- Connect the Evaluation board to your PC with a USB-to-USB-B connector (the "USB OTG" connection)
- Run the Tezi program `uuu recovery` to load the u-boot image from Tezi. Note you
can also substitute your own and use `uuu -f <file.imx>` to load it.
    - Note that you must use the non-"nand" version i.e. u-boot.imx, not u-boot-nand.imx

# Simple iMX6ULL Setup

A few instructions to give the Colibri enough network settings to function. The hostname, addresses and connman
service name below are examples; get the service name for your board from the output of `connmanctl services`
(it includes the board's MAC address).

```
hostnamectl hostname colibri17
connmanctl services
connmanctl config ethernet_00142d84bbbd_cable --domains diamond.ac.uk cs.diamond.ac.uk dc.diamond.ac.uk
connmanctl config ethernet_00142d84bbbd_cable --ipv4 manual 172.23.240.226 255.255.255.0 172.23.240.254
connmanctl config ethernet_00142d84bbbd_cable --nameservers 172.23.5.18 172.23.70.16 172.23.5.13
```
