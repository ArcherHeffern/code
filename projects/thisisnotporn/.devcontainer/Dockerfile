FROM ghcr.io/bare-devcontainer/uv:trixie

RUN echo "User is $USER"
USER root
RUN apt-get update && apt-get install -y tesseract-ocr libgl1 qtbase5-dev
USER dev