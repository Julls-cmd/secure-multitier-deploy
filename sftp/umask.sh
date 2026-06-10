#!/bin/sh

# Set sftp subsystem umask for uploaded files (0022 → 644 files, 755 dirs)
sed -i 's#^Subsystem sftp.*#Subsystem sftp internal-sftp -u 0022#' /etc/ssh/sshd_config

# Chroot dir must be owned by root (sshd requirement)
chown root:root /home/uploader
chmod 755 /home/uploader

# Upload subdir must be owned by the sftp user (UID/GID 101)
chown 101:101 /home/uploader/upload
chmod 750 /home/uploader/upload