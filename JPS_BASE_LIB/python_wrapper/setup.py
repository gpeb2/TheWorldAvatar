from setuptools import setup, find_packages
import os.path

setup(
    name='py4jps',
    version='1.0.0',
    author='Daniel Nurkowski',
    author_email='',
    license='',
    python_requires='>=3.5, <3.8',
    long_description=open('README.md').read(),
    packages=find_packages(exclude=('tests')),
    include_package_data=True
)