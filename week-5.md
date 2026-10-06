1. One sentence: how is a container different from a VM?
A container shares the host operating system kernel and isolates processes in user space, whereas a VM runs a complete guest operating system on virtualized hardware managed by a hypervisor.

2. Image vs container — what's the relationship?
An image is a static, read-only blueprint containing the application code and runtime dependencies, while a container is a live, running, stateful instance of that image.

3. In -p 9000:8000, which number is the browser and which is the app?
9000 is the host port accessed by the browser, and 8000 is the internal container port where the app listens.

4. Why put COPY requirements.txt / RUN pip install before COPY . .?
It leverages Docker layer caching to skip reinstalling dependencies on rebuilds whenever source code changes without dependency updates.

5. Your database container is deleted and recreated but the data survived. Why?
The data was persisted in a mounted Docker volume or host directory outside the container's ephemeral writable layer.

6. Two containers, same network. How does one reach the other, and why not localhost?
They reach each other using the target container's service name via Docker's embedded DNS, because localhost refers strictly to the network namespace of the caller's own container.